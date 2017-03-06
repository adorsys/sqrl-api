package de.adorsys.smartlogin.rest;

import de.adorsys.smartlogin.rest.vo.SqrlLoginCredentials;
import de.adorsys.smartlogin.rest.vo.SqrlLoginInfo;
import de.adorsys.smartlogin.spi.SqrlAccountProvider;
import de.adorsys.smartlogin.sqrl.SqrlCacheService;
import de.adorsys.smartlogin.sqrl.SqrlProcessData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("local")
@ApplicationScoped
public class LocalResource {
	
    private final static Logger LOG = LoggerFactory.getLogger(LocalResource.class);

    @Inject
    private SqrlCacheService cache;
    
    @Inject 
    private SqrlAccountProvider accountProvider;
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/check-login")
    public SqrlLoginInfo realmKey(SqrlLoginCredentials credentials) {

    	LOG.debug("Checking credentials for " + credentials);
    	SqrlLoginInfo sqrlLoginInfo = new SqrlLoginInfo();
    	
    	SqrlProcessData sqrlProcessData = cache.fetch(credentials.getNut());
    	cache.drop(credentials.getNut());
    	if(sqrlProcessData==null){
    		LOG.debug("No process data found for nut. " + credentials);
    		return sqrlLoginInfo;
    	}

    	
    	String accessToken = sqrlProcessData.getResponse().getAccessToken();
    	if(!StringUtils.equals(accessToken, credentials.getAccessTokenId())) {
    		LOG.debug("Access token not matching. Found: " + accessToken + " Required: " + credentials);
    		return sqrlLoginInfo;
    	}
    	
    	Map<String, String> data = sqrlProcessData.getPreparedData().getData();
    	if(data==null) {
    		LOG.debug("No login data provided for: " + credentials);
    		return sqrlLoginInfo;
    	}
    	
    	if (data.containsKey("accountId")){
    		String idpAccountId = accountProvider.getIdpAccountId(data.get("accountId"));
    		sqrlLoginInfo.setAccountId(idpAccountId);
    	}
    	return sqrlLoginInfo;
    }
}
