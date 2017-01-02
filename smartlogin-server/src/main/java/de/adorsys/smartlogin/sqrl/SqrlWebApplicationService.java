package de.adorsys.smartlogin.sqrl;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.representations.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.adorsys.smartlogin.idp.IdpKeyReader;
import de.adorsys.smartlogin.spi.SqrlAccountProvider;
import net.glxn.qrgen.QRCode;

/**
 * The web client's interface to the SQRL auth.<br>
 * <br>
 * Manages initialization and preparation as well as provides current status and
 * final authentication response.
 * 
 * @author mko
 */
@RequestScoped
public class SqrlWebApplicationService {
    private final static Logger LOG = LoggerFactory.getLogger(SqrlWebApplicationService.class);

    @Inject
    private SqrlCacheService cache;

    @Inject
    private SqrlAccountProvider sqrlAccountProvider;
    
    /**
     * Initializes a SQRL authentication session, adds sqrl-url data to the
     * given request and creates a process data cache for data transfer between
     * web and SQRL client.
     *
     * @param srequest
     *            the srequest
     */
    public void initPopulateWithSqrlData(HttpServletRequest srequest, UriInfo uriInfo) {
        SqrlProcessData sqrlProcessData = SqrlUtil.createSqrlProcessData();
        String nut = sqrlProcessData.getNut();
        SqrlUtil.addSqrlUrl(srequest, uriInfo, nut);
        cache.cache(sqrlProcessData.getNut(), sqrlProcessData);
    }

    /**
     * Initializes a SQRL authentication session, creates a process data cache
     * for data transfer between web and SQRL client.
     *
     * @return the sqrl-url as string
     */
    public String initCreateSqrlUrl(UriInfo uriInfo) {
        SqrlProcessData sqrlProcessData = SqrlUtil.createSqrlProcessData();
        String nut = sqrlProcessData.getNut();
        String result = SqrlUtil.buildSqrlUrl(nut, uriInfo);
        cache.cache(sqrlProcessData.getNut(), sqrlProcessData);
        return result;
    }

    /**
     * Creates a qr-code png representing the sqrl-url.
     */
    public byte[] createQrCode(String nut, UriInfo uriInfo) throws IOException {
        QRCode qr = SqrlUtil.createQrCodeImage(uriInfo, nut);
        return qr.stream().toByteArray();
    }

    /**
     * Allows to store web client data within the server for later use during
     * the SQRL client authentication within a simple key->value map.
     *
     * @param nut
     *            the nut identifiying the session to assign the data to
     * @param data
     *            the data containing a serializable Map<String,String>
     */
    public void prepare(String nut, SqrlAuthenticationPreparationData data) {
        SqrlProcessData sqrlProcessData = cache.fetch(nut);
        // We run into a null pointer here is the nut is no longer known to the server.
        // So what to do: 
        // Atl-1 : we return a 404 and tell call that nut is no longer in use.
        //     - Good but client might be mallicious
        //     - We will later need to use this to prevent DoS attacks. Caching 404 results.
        // Alt-2 : we do nothing.
        //      - 
        // Will start with atl-2
        if(sqrlProcessData==null) return;
        
        // verify idp token if any.
        String idpToken = data.getData().get("token");
        String realmName = data.getData().getOrDefault("realmName", "master");
        data.getData().clear();
		try {
			JsonWebToken jwt= verifyToken(idpToken, realmName);
			if(jwt!=null){
				String subject = jwt.getSubject();
				data.getData().put("userId", subject);
				data.getData().put("realmName", "master");
			}
		} catch (JWSInputException | IOException e) {
			LOG.error("Can not read or verify token", e);
		}

        sqrlProcessData.setPrepareData(data);
        sqrlProcessData.setState(SqrlState.PREPARED);
        cache.cache(sqrlProcessData.getNut(), sqrlProcessData);
    }
    
    private JsonWebToken verifyToken(String idpToken, String realmName) throws JWSInputException, FileNotFoundException, IOException{
        if(StringUtils.isNotBlank(idpToken)){
        	JWSInput jwsInput = new JWSInput(idpToken);
        	String keyId = jwsInput.getHeader().getKeyId();
        	String idpCertificate = IdpKeyReader.readIdpCertificate(keyId, realmName);
        	
        	// TODO use another library for better verification
        	boolean verify = jwsInput.verify(idpCertificate);
        	if(verify) return jwsInput.readJsonContent(JsonWebToken.class);
        }
        return null;
    }

    /**
     * Provides the current state of the SQRL authentication process.
     *
     * @param nut
     *            the nut identifiying the session to request the state from
     * @return the current sqrl state, if nut wasn't found returns {@see
     *         SqrlState.NONE} => 0
     */
    public SqrlState getSqrlState(String nut) {
        SqrlProcessData s = cache.fetch(nut);
        if (s == null) {
            return SqrlState.FAILED;
        }
        return s.getState();
    }

    /**
     * Provide response data.
     *
     * @param nut
     *            the nut
     * @return the sqrl response
     */
    public SqrlResponse provideResponseData(String nut) {
        SqrlProcessData s = cache.fetch(nut);
        if(s==null) return null;
        return s.getResponse();
//        cache.drop(nut);
//        return response;
    }

    /**
     * Exists sqrl identity for userId.
     *
     * @param nut the nut
     * @param userId the user id
     * @return true, if successful
     */
    public boolean existsSqrlIdentityFor(String nut, String userId){
        if(cache.existsProcessDataFor(nut) && sqrlAccountProvider.accountExistsBySqrlAccountId(userId)){
            return sqrlAccountProvider.sqrlIdentityExists(userId);
        }
        else{
            throw new SqrlAuthException(Response.Status.BAD_REQUEST);
        }
    }
    
    /**
     * Request sqrl deletion.
     *
     * @param nut the nut
     * @param userId the user id
     * @return the response
     */
    public void requestSqrlDeletion(@QueryParam("nut") String nut, @QueryParam("userid") String userId){
        if(cache.existsProcessDataFor(nut) && sqrlAccountProvider.accountExistsBySqrlAccountId(userId)){
            sqrlAccountProvider.deleteSqrlIdentityIfExists(userId);
        }
        else{
            throw new SqrlAuthException(Response.Status.BAD_REQUEST);
        }
    }
}
