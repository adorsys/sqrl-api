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
}
