package de.adorsys.smartlogin.account.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resource.RealmResourceProvider;

public class AccountResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;
	
	public AccountResourceProvider(KeycloakSession session) {
		super();
		this.session = session;
	}

	@Override
	public void close() {
	}

	@Override
	public Object getResource() {
		return this;
	}

	/**
	 * Returns the user id based on the login name of the user.
	 * @param userLogin
	 * @return
	 */
    @GET
    @Path("/users/${userLogin}")
    @Produces(MediaType.TEXT_PLAIN)
    public String get(@PathParam("userLogin") String userLogin) {
    	RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserByUsername(userLogin, realm);
        if (user == null) {
            return "";
        }
        return user.getId();
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "Account resource installed";
    }
}
