package de.adorsys.smartlogin.sqrl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import de.adorsys.smartlogin.spi.IdpAccountProvider;

@ApplicationScoped
public class IdpAccountRest implements IdpAccountProvider {
	
	private WebTarget userFromLoginTemplate;
	
	
	@PostConstruct
	public void postConstruct(){
		String url = System.getenv("idp_account_service_endpoint");
		if(url==null)url="http://sqrl:8081/auth/realm/master/bc-account";
		UriBuilder users_UserLogin_uri_template = UriBuilder.fromUri(url).fragment("users").fragment("{userLogin}");
		Client client = ClientBuilder.newClient();
		userFromLoginTemplate = client.target(users_UserLogin_uri_template);
	}

	@Override
	public boolean idpAccountExists(String userLogin) {
		Response response = userFromLoginTemplate.resolveTemplate("userLogin", userLogin).request(MediaType.TEXT_PLAIN_TYPE).get();
		if(response.getStatus()!=200){
			Object entity = response.getEntity();
			if(entity!=null){
				String str = entity.toString();
				if(str.length()>0) return true;
			}
			return false;
		} else if (response.getStatus()==404){// not found
			return false;
		} else {
			throw new IllegalStateException("could not read request" + response.getStatus());
		}
	}

}
