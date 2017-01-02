package de.adorsys.smartlogin.directgrant.sqrl;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class SqrlAccountAdapter {
	
	private WebTarget userFromLoginTemplate;
	
	public SqrlAccountAdapter(){
		String url = System.getenv("sqrl_account_service_endpoint");
		if(url==null)url="http://sqrl:8081/smartlogin-server/rest/local/check-login";
		UriBuilder users_UserLogin_uri_template = UriBuilder.fromUri(url);
		Client client = ClientBuilder.newClient();
		userFromLoginTemplate = client.target(users_UserLogin_uri_template);
	}

	public String checkSqrlAccount(String nut, String accessTokenId) {
		SqrlLoginCredentials sqrlLoginCredentials = new SqrlLoginCredentials();
		sqrlLoginCredentials.setNut(nut);
		sqrlLoginCredentials.setAccessTokenId(accessTokenId);
		Entity<SqrlLoginCredentials> entity = Entity.entity(sqrlLoginCredentials, MediaType.APPLICATION_JSON_TYPE);
		SqrlLoginInfo sqrlLoginInfo = userFromLoginTemplate.request(MediaType.APPLICATION_JSON_TYPE).post(entity, SqrlLoginInfo.class);
		if(sqrlLoginInfo==null) return null;
		return sqrlLoginInfo.getAccountId();
	}

}
