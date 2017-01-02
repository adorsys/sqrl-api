package de.adorsys.smartlogin.directgrant;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.directgrant.AbstractDirectGrantAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

import de.adorsys.smartlogin.directgrant.sqrl.SqrlAccountAdapter;

public class ValidateSQRL extends AbstractDirectGrantAuthenticator {

    public static final String PROVIDER_ID = "smartlogin-drct-grnt-sqrl-login";
    
    SqrlAccountAdapter sqrlAccountAdapter = new SqrlAccountAdapter();

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        String nut = inputData.getFirst("nut");
        String accessTokenId = inputData.getFirst("accessTokenId");

        if ((nut==null || nut.trim().length()==0) && (accessTokenId==null || accessTokenId.trim().length()==0)) {
            context.success();
            return;
        }
        
        // if nut or access token id is provided, means client is performing an sqrl login.
        String sqrlAccount = sqrlAccountAdapter.checkSqrlAccount(nut, accessTokenId);
        
        if(sqrlAccount==null || sqrlAccount.trim().length()==0){
        	context.getEvent().error(Errors.USER_NOT_FOUND);
        	Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Invalid sqrl login data");
        	context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
        	return;
        }

        context.getEvent().detail(Details.IDENTITY_PROVIDER_USERNAME, sqrlAccount);

        UserModel user = context.getSession().users().getUserById(sqrlAccount, context.getRealm());

        if (user == null) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        if (!user.isEnabled()) {
            context.getEvent().user(user);
            context.getEvent().error(Errors.USER_DISABLED);
            Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Account disabled");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        if (context.getRealm().isBruteForceProtected()) {
            if (context.getProtector().isTemporarilyDisabled(context.getSession(), context.getRealm(), user)) {
                context.getEvent().user(user);
                context.getEvent().error(Errors.USER_TEMPORARILY_DISABLED);
                Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Account temporarily disabled");
                context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
                return;
            }
        }
        
//        context.getClientSession().getUserSession().setNote("sqrl-login", accessTokenId);
        context.setUser(user);
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    public String getDisplayType() {
        return "Smartlogin SQRL Login";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return "Validates sqrl credentials supplied as 'nut and accessTokenId' as form parameter in direct grant request";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new LinkedList<>();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
