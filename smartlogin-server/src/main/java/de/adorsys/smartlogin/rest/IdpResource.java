package de.adorsys.smartlogin.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.adorsys.smartlogin.envutils.EnvProperties;
import de.adorsys.smartlogin.idp.IdpKeyReader;
import de.adorsys.smartlogin.idp.IdpUtils;

/**
 * Created by alexg on 07.12.16.
 */
@Path("idp")
@ApplicationScoped
public class IdpResource {
	
    private final static Logger LOG = LoggerFactory.getLogger(IdpResource.class);
	private static final String EXTERNAL_IDP_HOST_AND_PORT = "EXTERNAL_IDP_HOST_AND_PORT";
	private static final String IDPHOST = "idphost";
	private static final String IDPPORT = "idpport";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{realmName}/realm-keys")
    public Response realmKey(@PathParam("realmName") String realmName) {
    	try {
    		String realmKeyFileContent = IdpKeyReader.realmKey(realmName);
			return Response.ok().entity(realmKeyFileContent).build();
    	} catch (FileNotFoundException f){
    		return Response.status(Status.NOT_FOUND).build();
    	} catch(IOException e){
			LOG.error("Error reading key file for " + realmName, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    	}
    }

    /**
     * Return the client configuration for the specified client. This response contains the url of the 
     * identity provider. 
     * 
     * CORS concerns
     * In order for the client application (generally a web browser) to send a token request to the referenced 
     * idp. we need to include a CORS header to the rsponse of this request.
     * 
     * @param clientId
     * @return
     */
    @GET
    @Path("/{clientId}/client-config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientConfig(@PathParam("clientId") String clientId) {
    	String clientConfigFileName = clientId+".json";
    	
    	String externaldpHostAndPort = EnvProperties.getEnvProp(EXTERNAL_IDP_HOST_AND_PORT, false);
    	String internalIdpHost=EnvProperties.getEnvProp(IDPHOST, "localhost");
    	String internalIdpPort=EnvProperties.getEnvProp(IDPPORT, "8080");
    	String internalIdpHostAndPort = internalIdpHost + (internalIdpPort!=null?":"+internalIdpPort:"");
    	Map<String, String> replace = new HashMap<>();
    	replace.put(internalIdpHostAndPort, externaldpHostAndPort);
    	
    	String clientConfigContent = null; 
    	try {
    		clientConfigContent = IdpUtils.readFile(clientConfigFileName, replace);
    		if(StringUtils.isBlank(clientConfigContent)){
    			return Response.status(Status.NO_CONTENT).build();
    		}
    	} catch (FileNotFoundException f){
    		return Response.status(Status.NOT_FOUND).build();
    	} catch(IOException e){
			LOG.error("Error reading file " + clientConfigFileName, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    	}
    	
    	ResponseBuilder builder = Response.ok().entity(clientConfigContent);
    	
    	return builder.build();
    }
}
