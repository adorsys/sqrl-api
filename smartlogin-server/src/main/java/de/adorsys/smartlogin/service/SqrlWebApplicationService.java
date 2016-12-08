package de.adorsys.smartlogin.service;

import de.adorsys.smartlogin.provider.AccountProvider;
import net.glxn.qrgen.QRCode;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;

/**
 * The web client's interface to the SQRL auth.<br>
 * <br>
 * Manages initialization and preparation as well as provides current status and
 * final authentication response.
 * 
 * @author mko
 */
@RequestScoped
public class SqrlWebApplicationService {

    @Inject
    private SqrlCacheService cache;

    @Inject
    private AccountProvider accountProvider;
    
    /**
     * Initializes a SQRL authentication session, adds sqrl-url data to the
     * given request and creates a process data cache for data transfer between
     * web and SQRL client.
     *
     * @param srequest
     *            the srequest
     */
    public void initPopulateWithSqrlData(HttpServletRequest srequest, UriInfo uriInfo) {
        SqrlProcessData sqrlProcessData = SqrlUtil.createSqrlProcessData();
        String nut = sqrlProcessData.getNut();
        SqrlUtil.addSqrlUrl(srequest, uriInfo, nut);
        cache.cache(sqrlProcessData.getNut(), sqrlProcessData);
    }

    /**
     * Initializes a SQRL authentication session, creates a process data cache
     * for data transfer between web and SQRL client.
     *
     * @return the sqrl-url as string
     */
    public String initCreateSqrlUrl(UriInfo uriInfo) {
        SqrlProcessData sqrlProcessData = SqrlUtil.createSqrlProcessData();
        String nut = sqrlProcessData.getNut();
        String result = SqrlUtil.createSqrlUrl(uriInfo, nut);
        cache.cache(sqrlProcessData.getNut(), sqrlProcessData);
        return result;
    }

    /**
     * Creates a qr-code png representing the sqrl-url.
     */
    public File createQrCode(String nut, UriInfo uriInfo, HttpServletResponse response) throws IOException {
        QRCode qr = SqrlUtil.createQrCodeImage(uriInfo, nut);
        File file = qr.file();
        response.setContentType("image/png");
        response.setContentLength((int) file.getTotalSpace());
        return file;
    }

    /**
     * Allows to store web client data within the server for later use during
     * the SQRL client authentication within a simple key->value map.
     *
     * @param nut
     *            the nut identifiying the session to assign the data to
     * @param data
     *            the data containing a serializable Map<String,String>
     */
    public void prepare(String nut, SqrlAuthenticationPreparationData data) {
        SqrlProcessData sqrlProcessData = cache.fetch(nut);
        sqrlProcessData.setPrepareData(data);
        sqrlProcessData.setState(SqrlState.PREPARED);
        cache.cache(sqrlProcessData.getNut(), sqrlProcessData);
    }

    /**
     * Provides the current state of the SQRL authentication process.
     *
     * @param nut
     *            the nut identifiying the session to request the state from
     * @return the current sqrl state, if nut wasn't found returns {@see
     *         SqrlState.NONE} => 0
     */
    public SqrlState getSqrlState(String nut) {
        SqrlProcessData s = cache.fetch(nut);
        if (s == null) {
            return SqrlState.NONE;
        }
        return s.getState();
    }

    /**
     * Provide response data.
     *
     * @param nut
     *            the nut
     * @return the sqrl response
     */
    public SqrlResponse provideResponseData(String nut) {
        SqrlProcessData s = cache.fetch(nut);
        SqrlResponse response = s.getResponse();
        cache.drop(nut);
        return response;
    }

    /**
     * Exists sqrl identity for userId.
     *
     * @param nut the nut
     * @param userId the user id
     * @return true, if successful
     */
    public boolean existsSqrlIdentityFor(String nut, String userId){
        if(cache.existsProcessDataFor(nut) && accountProvider.accountExists(userId)){
            return accountProvider.sqrlIdentityExists(userId);
        }
        else{
            throw new SqrlAuthException(Response.Status.BAD_REQUEST);
        }
    }
    
    /**
     * Request sqrl deletion.
     *
     * @param nut the nut
     * @param userId the user id
     * @return the response
     */
    public void requestSqrlDeletion(@QueryParam("nut") String nut, @QueryParam("userid") String userId){
        if(cache.existsProcessDataFor(nut) && accountProvider.accountExists(userId)){
            accountProvider.deleteSqrlIdentityIfExists(userId);
        }
        else{
            throw new SqrlAuthException(Response.Status.BAD_REQUEST);
        }
    }
}
