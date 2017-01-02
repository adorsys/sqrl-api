package de.adorsys.smartlogin.directgrant;

import org.keycloak.authentication.AuthenticationFlowContext;

public class ValidateUsername extends org.keycloak.authentication.authenticators.directgrant.ValidateUsername {

    private static final String PROVIDER_ID = "smartlogin-drct-grnt-username";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // SQRL modification.
        if(context.getUser()!=null && context.getClientSession().getUserSessionNotes().containsKey("sqrl-login")){
        	context.success();
        	return;
        }
        super.authenticate(context);
    }
    
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
    
    @Override
    public String getHelpText() {
        return "Validates the username supplied as a 'username' form parameter in direct grant request";
    }

    @Override
    public String getDisplayType() {
        return "Smartlogin Username Validation";
    }
}