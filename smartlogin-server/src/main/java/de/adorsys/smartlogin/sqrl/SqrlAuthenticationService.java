package de.adorsys.smartlogin.sqrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import de.adorsys.smartlogin.db.SqrlAccount;
import de.adorsys.smartlogin.spi.SqrlAccountProvider;
import net.vrallev.java.sqrl.SqrlException;
import net.vrallev.java.sqrl.SqrlProtocol;
import net.vrallev.java.sqrl.body.ServerParameter;
import net.vrallev.java.sqrl.body.SqrlClientBody;
import net.vrallev.java.sqrl.body.SqrlClientBodyParser;
import net.vrallev.java.sqrl.body.SqrlServerBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SQRL client's interface to the SQRL auth.<br>
 * <br>
 * Provides SQRL request handling based on SQRL protocol from Steve Gibson and others. The specification is hosted at
 * {@link https://www.grc.com/sqrl/sqrl.htm}.<br>
 * <br>
 * Uses SQRL protocol Java implementation from Ralf Wondratschek for adorsys GmbH & Co KG, released under Apache License,
 * Version 2.0. Hosted at github {@link https://github.com/vRallev/SQRL-Protocol}.<br>
 * <br>
 * The request handling sequence is:<br>
 * - Parsing<br>
 * - - specified fields 'qry', 'lnk' and 'ask' are ignored. <br>
 * - - {@see ../sqrl-protocol/src/main/java/net/vrallev/java/sqrl/body/ServerParameter.java:line38}<br>
 * - Decoding<br>
 * - Verifying<br>
 * - Bulk or Single Command Execution<br>
 * - - currently supported commands: 'login, create, setkey, setlock'<br>
 * - - for missing commands have a look at the specs and @see SqrlCommands<br>
 * - - as 'create' implies setting key and lock, additional 'setkey' or 'setlock' commands are ignored if 'create' command is
 * found <br>
 * - Error Handling<br>
 * - Response Creation<br>
 *
 * @author mko
 */
@RequestScoped
public class SqrlAuthenticationService {

    private final static Logger LOG = LoggerFactory.getLogger(SqrlAuthenticationService.class);

    private final static String FRIENDLY_SERVER_NAME = "smartlogin";
    private final static String PREPARED_FIELD_USER_LOGIN = "userId";

    @Inject
    private SqrlCacheService cache;

    @Inject
    private SqrlAccountProvider sqrlAccountProvider;

    @Inject
    private SqrlRequestKeyIdentityDepot keys;

    /**
     * SQRL authentication entrypoint.
     * <p/>
     * Trys to extract expected SQRL data from HttpRequest, parse it into an insecure client body instance without checking
     * signatures. That will lead to a possible decoded 'nut' value.
     * <p/>
     * If we have the 'nut' AND an according 'ProcessData'-instance as data spi between Web and SQRL client we can start
     * SQRL auth itself.
     */
    public void handleSqrlRequest(HttpServletRequest req, HttpServletResponse resp) throws SqrlAuthException {
        LOG.debug("Start and prepare sqrl client http request");

        Map<String, String> map = SqrlUtil.extractClientParameterMap(req);

        SqrlClientBodyParser clientBodyParser = SqrlProtocol.instance().readSqrlClientBody().from(map);

        SqrlClientBody insecureClientBody;
        try {
            insecureClientBody = clientBodyParser.execute();
        } catch (SqrlException e1) {
            throw new SqrlAuthException(Response.Status.BAD_REQUEST);
        }

        String nut = insecureClientBody.getServerParameter().getNutDecoded();

        if (!cache.existsProcessDataFor(nut)) {
            LOG.error("SQRL client tried to use SQRL Auth without process data to provide.");
            throw new SqrlAuthException(Response.Status.FORBIDDEN);
        }

        SqrlServerBody resultBody = decodeRequest(clientBodyParser, insecureClientBody, nut);

        // deliver answer to sqrl client
        try {
            if (resultBody != null) {
                resp.setStatus(200);
                resp.getOutputStream().write(resultBody.getBodyEncoded().getBytes("US-ASCII"));
                resp.getOutputStream().flush();
            } else {
                updateState(nut, SqrlState.FAILED);
                resp.sendError(400, "Malformed request could not be processed. Sorry Dude!");
            }
        } catch (IOException e) {
            throw new SqrlAuthException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Decode the SQRL request. Now that we have a valid nut, we can provide information from SQRL client authentication
     * process to the Web client.ƒƒ
     */
    private SqrlServerBody decodeRequest(SqrlClientBodyParser clientBodyParser, SqrlClientBody insecureClientBody,
                                         String nut) {
        LOG.debug("Entered decoding");

        keys = new SqrlRequestKeyIdentityDepot();

        keys.idk = insecureClientBody.getClientParameter().getIdentityKeyDecoded();

        keys.pidk = insecureClientBody.getClientParameter().getPreviousIdentityKeyDecoded();

        byte[] serverUnlockKey = sqrlAccountProvider.fetchServerUnlockKey(keys.idk);
        byte[] verifyUnlockKey = sqrlAccountProvider.fetchVerifyUnlockKey(keys.idk);

        if (serverUnlockKey != null && verifyUnlockKey != null) {
            clientBodyParser.withStoredKeys(serverUnlockKey, verifyUnlockKey);
        }

        return verifyClientRequestBody(clientBodyParser, nut);
    }

    /**
     * Verify the client request body. All signatures are going to be validated against provided (not yet created) or stored
     * keys.
     * <p/>
     * Signing and validating with Ecc25519 based DHKA.
     */
    private SqrlServerBody verifyClientRequestBody(SqrlClientBodyParser clientBodyParser, String nut) {
        LOG.debug("Verify client body signatures");
        SqrlClientBody clientBody = null;
        try {
            clientBody = clientBodyParser.verified();
        } catch (SqrlException e) {
            LOG.error("Client request signatures invalid");
            updateState(nut, SqrlState.FAILED);
            throw new SqrlAuthException(Response.Status.BAD_REQUEST);
        }

        return processCommands(clientBody, nut);
    }

    /**
     * Process all commands in the order provided by the client request.
     * <p/>
     * When all are finished, one answer is returned to the client that combines the results transaction information flags.
     */
    private SqrlServerBody processCommands(SqrlClientBody clientBody, String nut) {
        LOG.info("Start commands execution");
        List<String> commands = clientBody.getClientParameter().getCommands();

        List<SqrlState> stateCollector = new ArrayList<>();

        List<SqrlServerBody> serverBodies = executeCommands(clientBody, commands, nut, stateCollector);

        // build combined sqrl server answer
        SqrlServerBody resultBody = null;
        if (serverBodies.size() > 1) {
            int[] tifArray = new int[serverBodies.size()];
            for (int i = 0; i < serverBodies.size(); i++) {
                int tif = serverBodies.get(i).getServerParameter().getTransactionInformationFlag();
                tifArray[i] = tif;
            }
            resultBody = SqrlProtocol.instance().answerClient(clientBody, tifArray).withServerFriendlyName(FRIENDLY_SERVER_NAME).create().asSqrlServerBody();
        } else {
            resultBody = serverBodies.get(0);
        }

        updateState(nut, mostSignificantState(stateCollector));
        return resultBody;
    }

    /**
     * The State with the highest ordinal is considered the most significant.
     */
    private SqrlState mostSignificantState(Collection<SqrlState> states) {
        SqrlState result = SqrlState.SUCCEEDED;
        for (SqrlState state : states) {
            if (state.ordinal() > result.ordinal()) result = state;
        }
        return result;
    }

    private List<SqrlServerBody> executeCommands(SqrlClientBody clientBody, List<String> commands, String nut, List<SqrlState> stateCollector) {
        List<SqrlServerBody> serverBodies = new ArrayList<>();
        for (String command : commands) {
            // skip setkey, setlock - is implied in create
            if (commands.contains(SqrlCommands.CREATE) && (command.equals(SqrlCommands.SET_KEY) || command.equals(SqrlCommands.SET_LOCK))) {
                continue;
            }
            LOG.info("Executing command " + command);
            SqrlServerBody singleServerBody = executeSingleCommand(clientBody, command.toLowerCase(), nut, stateCollector);
            if (singleServerBody != null) {
                serverBodies.add(singleServerBody);
            }
            LOG.info("Command " + command + " finished");
        }
        return serverBodies;
    }

    /**
     * Executes single command.
     * <p/>
     * implemented: login, create, setkey, setlock return their command result
     * <p/>
     * <p/>
     * not implemented: disable, enable, delete, logme, logoff return always failure
     */
    private SqrlServerBody executeSingleCommand(SqrlClientBody clientBody, String command, String nut, List<SqrlState> stateCollector) {
        if (command.equals(SqrlCommands.LOGIN)) {
            return onLogin(clientBody, nut, stateCollector);
        } else if (command.contains(SqrlCommands.CREATE)) {
            return onCreate(clientBody, nut, stateCollector);
        } else if (command.contains(SqrlCommands.SET_KEY)) {
            return onSetKey(clientBody, nut, stateCollector);
        } else if (command.equals(SqrlCommands.SET_LOCK)) {
            return onSetLock(clientBody, nut, stateCollector);
        }
        LOG.error("Invalid or not yet implemented command '" + command + "' requested by client");
        updateState(nut, SqrlState.FAILED);
        return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.COMMAND_FAILED, ServerParameter.SQRL_FAILURE)
                .withServerFriendlyName(FRIENDLY_SERVER_NAME).create().asSqrlServerBody();
    }

    private SqrlServerBody onLogin(SqrlClientBody clientBody, String nut, List<SqrlState> stateCollector) {
        byte[] storedServerUnlockKey = sqrlAccountProvider.fetchServerUnlockKey(keys.idk);
        byte[] storedVerifyUnlockKey = sqrlAccountProvider.fetchVerifyUnlockKey(keys.idk);

        if (storedServerUnlockKey != null && storedVerifyUnlockKey != null) {

            String accountId = this.getAccountId(keys.idk);
            if (accountId == null) {
                accountId = this.getAccountId(keys.pidk);
            }

            SqrlProcessData processData = cache.fetch(nut);

            //TODO IDP/Keycloak handling
            if (accountId != null) {
//                Token accessToken = CreateAndStoreTokenTask.createAndStoreToken(TokenType.REQUEST, accountId, client, "", services.getTokenRepository());

                SqrlResponse sqrlResponse = new SqrlResponse("test-token", 100000l);

                processData.setResponse(sqrlResponse);
                cache.cache(nut, processData);

                stateCollector.add(SqrlState.LOGIN_SUCCEEDED);

                return SqrlProtocol.instance()
                        .answerClient(clientBody, ServerParameter.ID_MATCH, ServerParameter.USER_LOGGED_IN)
                        .withServerFriendlyName(FRIENDLY_SERVER_NAME).withStoredKeys(storedServerUnlockKey, storedVerifyUnlockKey).create().asSqrlServerBody();
            } else {
                LOG.error("Could not retrieve valid application client or provided identity key was not found");
            }
        } else {
            //if user id is known, we have preparation to create account
            LOG.warn("Default login not accepted, but detecting account creation prepared");
            SqrlProcessData processData = cache.fetch(nut);
            if (processData.getPreparedData().findValue(PREPARED_FIELD_USER_LOGIN) != null) {
                return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.SQRL_ACCOUNT_CREATION_ALLOWED).withServerFriendlyName(FRIENDLY_SERVER_NAME)
                        .create().asSqrlServerBody();
            }
        }
        stateCollector.add(SqrlState.FAILED);
        return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.COMMAND_FAILED, ServerParameter.SQRL_FAILURE).withServerFriendlyName(FRIENDLY_SERVER_NAME)
                .create().asSqrlServerBody();
    }

    private SqrlServerBody onCreate(SqrlClientBody clientBody, String nut, List<SqrlState> stateCollector) {
        SqrlProcessData processData = cache.fetch(nut);

        if (processData == null) {
            stateCollector.add(SqrlState.FAILED);
            return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.COMMAND_FAILED)
                    .withServerFriendlyName(FRIENDLY_SERVER_NAME).create().asSqrlServerBody();
        }

        String userLogin = processData.getPreparedData().findValue(PREPARED_FIELD_USER_LOGIN);

        byte[] storedServerUnlockKey = sqrlAccountProvider.fetchServerUnlockKey(keys.idk);
        byte[] storedVerifyUnlockKey = sqrlAccountProvider.fetchVerifyUnlockKey(keys.idk);

        if (storedServerUnlockKey == null && storedVerifyUnlockKey == null) {
            if (userLogin != null) {

                //TODO IDP/Keycloak handling account must exist - lazy creating sqrl account?
                if (!sqrlAccountProvider.accountExistsByIdpAccountId(userLogin)) {
                    sqrlAccountProvider.createSqrlAccount(new SqrlAccount().idpAccountId(userLogin));
                }

                byte[] serverUnlockKey = clientBody.getClientParameter().getServerUnlockKeyDecoded();
                byte[] verifyUnlockKey = clientBody.getClientParameter().getVerifyUnlockKeyDecoded();

                LOG.info("onCreate: Trying to insert sqrl keys");
                if (sqrlAccountProvider.insertSqrlKeys(userLogin, keys.idk, serverUnlockKey, verifyUnlockKey)) {
                    stateCollector.add(SqrlState.CREATE_SUCCEEDED);
                    return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.ID_MATCH).withServerFriendlyName(FRIENDLY_SERVER_NAME)
                            .withStoredKeys(serverUnlockKey, verifyUnlockKey).create().asSqrlServerBody();
                } else {
                    LOG.error("Saving SQRL identity data to db failed.");
                }

            } else {
                LOG.error("onCreate: UserId was not prepared.");
            }
        } else {
            LOG.error("onCreate: Found existing keys. Client should use 'setkey' or 'setlock'");
        }

        stateCollector.add(SqrlState.FAILED);
        return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.COMMAND_FAILED, ServerParameter.SQRL_FAILURE)
                .withServerFriendlyName(FRIENDLY_SERVER_NAME).create().asSqrlServerBody();
    }

    private SqrlServerBody onSetKey(SqrlClientBody clientBody, String nut, List<SqrlState> stateCollector) {
        byte[] serverUnlockKey = clientBody.getClientParameter().getServerUnlockKeyDecoded();
        byte[] verifyUnlockKey = clientBody.getClientParameter().getVerifyUnlockKeyDecoded();
        byte[] previousIdentityKey = clientBody.getClientParameter().getPreviousIdentityKeyDecoded();

        if (previousIdentityKey != null) {
            if (sqrlAccountProvider.updateSqrlKeys(previousIdentityKey, keys.idk, serverUnlockKey, verifyUnlockKey)) {
                stateCollector.add(SqrlState.SUCCEEDED);
                return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.ID_MATCH).withServerFriendlyName(FRIENDLY_SERVER_NAME)
                        .withStoredKeys(serverUnlockKey, verifyUnlockKey).create().asSqrlServerBody();
            }
        }

        stateCollector.add(SqrlState.FAILED);
        return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.COMMAND_FAILED, ServerParameter.SQRL_FAILURE)
                .withServerFriendlyName(FRIENDLY_SERVER_NAME).create().asSqrlServerBody();
    }

    private SqrlServerBody onSetLock(SqrlClientBody clientBody, String nut, List<SqrlState> stateCollector) {
        byte[] storedServerUnlockKey = sqrlAccountProvider.fetchServerUnlockKey(keys.idk);
        byte[] storedVerifyUnlockKey = sqrlAccountProvider.fetchVerifyUnlockKey(keys.idk);

        byte[] serverUnlockKey = clientBody.getClientParameter().getServerUnlockKeyDecoded();
        byte[] verifyUnlockKey = clientBody.getClientParameter().getVerifyUnlockKeyDecoded();
        byte[] unlockRequestSignature = clientBody.getUnlockRequestSignatureDecoded();

        boolean valid = false;
        boolean error = false;

        // nothing set yet -> ok
        if (storedServerUnlockKey == null && storedVerifyUnlockKey == null) {
            valid = true;
        }
        // client provides urs
        else if (unlockRequestSignature != null) {
            if (SqrlProtocol.instance().getEccProvider().isValidSignature(storedServerUnlockKey, unlockRequestSignature, storedVerifyUnlockKey)) {
                valid = true;
            } else {
                error = true;
            }
        } else {
            error = true;
        }

        // no error occured
        if (!error) {
            // update allowed
            if (valid) {
                // update success
                if (!sqrlAccountProvider.updateSqrlServerAndVerifyUnlockKey(keys.idk, serverUnlockKey, verifyUnlockKey)) {
                    LOG.error("Could not persist new lock key. Ensure correct identity key.");
                    error = true;
                }
            }
            // update forbidden
            else {
                LOG.error("Change lock key forbidden.");
                error = true;
            }
        }

        if (error) {
            stateCollector.add(SqrlState.FAILED);
            return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.COMMAND_FAILED, ServerParameter.SQRL_FAILURE)
                    .withServerFriendlyName(FRIENDLY_SERVER_NAME).create().asSqrlServerBody();
        } else {
            stateCollector.add(SqrlState.SUCCEEDED);
            return SqrlProtocol.instance().answerClient(clientBody, ServerParameter.ID_MATCH).withServerFriendlyName(FRIENDLY_SERVER_NAME).create()
                    .asSqrlServerBody();
        }
    }

    // helper //

    private String getAccountId(byte[] identityKey) {
        return sqrlAccountProvider.checkIdentity(identityKey);
    }

    private void updateState(String nut, SqrlState state) {
        SqrlProcessData cachedSession = cache.fetch(nut);
        cachedSession.setState(state);
        cache.cache(nut, cachedSession);
        LOG.info("Providing state update: " + state.name());
    }

}
