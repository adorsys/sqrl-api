package de.adorsys.smartlogin.rest;

import de.adorsys.smartlogin.sqrl.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Created by alexg on 07.12.16.
 */
@Path("auth")
@ApplicationScoped
public class SqrlResource {

    @Inject
    private SqrlWebApplicationService sqrlService;

    @Inject
    private SqrlAuthenticationService sqrlAuthenticationService;

    @GET
    @Produces("text/plain")
    @Path("/sqrl-uri")
    public String createSqrlUri(@Context UriInfo uriInfo) {
        return sqrlService.initCreateSqrlUrl(uriInfo);
    }

    @GET
    @Path("/sqrl-qr-code")
    @Produces("image/png")
    public Response createQrCode(@QueryParam("nut") String nut, @Context UriInfo uriInfo, @Context HttpServletResponse response) throws IOException {
        return Response.ok(sqrlService.createQrCode(nut, uriInfo)).build();
    }

    @POST
    @Path("/sqrl-prepare")
    @Consumes(MediaType.APPLICATION_JSON)
    public void prepare(@QueryParam("nut") String nut, SqrlAuthenticationPreparationData data){
        sqrlService.prepare(nut, data);
    }
    
    @GET
    @Produces("text/plain")
    @Path("/sqrl-state")
    public String getSqrlState(@QueryParam("nut") String nut){
        return sqrlService.getSqrlState(nut).name();
    }

    @GET
    @Path("/sqrl-response")
    @Produces(MediaType.APPLICATION_JSON)
    public SqrlResponse provideResponseData(@QueryParam("nut") String nut){
        return sqrlService.provideResponseData(nut);
    }

    @GET
    @Path("/sqrl-exists")
    public boolean existsSqrlIdentityFor(@QueryParam("nut") String nut, @QueryParam("userid") String userId){
        return sqrlService.existsSqrlIdentityFor(nut, userId);
    }

    @DELETE
    @Path("/sqrl")
    public void requestSqrlDeletion(@QueryParam("nut") String nut, @QueryParam("userid") String userId){
        sqrlService.requestSqrlDeletion(nut, userId);
    }

    @POST
    @Path("/sqrl")
    @Consumes("application/x-www-form-urlencoded")
    public void handleSqrlRequest(@Context HttpServletRequest request, @Context HttpServletResponse response) throws SqrlAuthException {
        sqrlAuthenticationService.handleSqrlRequest(request, response);
    }
}
