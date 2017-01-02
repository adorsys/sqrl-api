package de.adorsys.smartlogin.directgrant;

import javax.ws.rs.core.MultivaluedMap;

import org.keycloak.authentication.AuthenticationFlowContext;

public class ValidatePassword extends org.keycloak.authentication.authenticators.directgrant.ValidatePassword {

    private static final String PROVIDER_ID = "smartlogin-drct-grnt-password";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        String nut = inputData.getFirst("nut");
        String accessTokenId = inputData.getFirst("accessTokenId");

        if ((nut!=null || accessTokenId!=null) && context.getUser()!=null) {
            context.success();
            return;
        }

        super.authenticate(context);
    }

    @Override
    public String getDisplayType() {
        return "Smartlogin Password";
    }
    @Override
    public String getHelpText() {
        return "Validates the password supplied as a 'password' form parameter in direct grant request";
    }
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
