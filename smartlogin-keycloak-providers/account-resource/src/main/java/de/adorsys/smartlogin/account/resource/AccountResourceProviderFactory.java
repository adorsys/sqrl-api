package de.adorsys.smartlogin.account.resource;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class AccountResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "bc-account";
	
	@Override
	public void close() {
	}

	@Override
	public RealmResourceProvider create(KeycloakSession session) {
		return new AccountResourceProvider(session);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(Scope config) {
	}

	@Override
	public void postInit(KeycloakSessionFactory arg0) {
	}
}
