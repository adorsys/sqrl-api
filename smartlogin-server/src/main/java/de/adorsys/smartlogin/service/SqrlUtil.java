package de.adorsys.smartlogin.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

/**
 * Utilities for sqrl url & qr-code generation etc.
 * 
 * @author mko
 */
public class SqrlUtil {

    public static final String ATTR_SQRL_URI = "sqrlUri";
    public static final String ATTR_SQRL_QR = "sqrlQr";
    private static final int SIDE_LENGTH = 600;

    /**
     * Create a new unique @see SqrlProcessData.
     *
     * @return the sqrl session
     */
    public static SqrlProcessData createSqrlProcessData() {
        SqrlProcessData session = new SqrlProcessData();
        String nut = SqrlNutFactory.createNut();
        session.setNut(nut);
        return session;
    }

    /**
     * Wrapper for @see SqrlUtil.buildSqrlUrl(nut, srequest) to provide later parameter inclusion.
     */
    public static String createSqrlUrl(UriInfo uriInfo, String nut) {

        String sqrlUrl = SqrlUtil.buildSqrlUrl(nut, uriInfo);

        return sqrlUrl;
    }

    /**
     * Add sqrl-uri and qr data as attributes to given HttpServletRequest.
     *
     * @param srequest
     *            the srequest
     * @param nut
     *            the nut
     */
    public static void addSqrlUrl(HttpServletRequest srequest, UriInfo uriInfo, String nut) {

        String sqrlUrl = SqrlUtil.buildSqrlUrl(nut, uriInfo);

        srequest.setAttribute(ATTR_SQRL_URI, sqrlUrl);
        srequest.setAttribute(ATTR_SQRL_QR, SqrlUtil.buildQrUrl(srequest, nut));
    }

    /**
     * Creates the qr code image.
     */
    public static QRCode createQrCodeImage(UriInfo uriInfo, String nut) throws IOException {

        String sqrlUri = SqrlUtil.buildSqrlUrl(nut, uriInfo);

        QRCode qrCode = QRCode.from(sqrlUri).to(ImageType.PNG).withSize(SIDE_LENGTH, SIDE_LENGTH);

        return qrCode;
    }

    /**
     * Builds the sqrl url containing the nut.
     * Example: "qrl://192.168.178.49:8080/bouncer.server/rest/auth/sqrl?nut=znLEgS5BT6m2qwC8IPmsPITKaqc"
     */
    private static String buildSqrlUrl(String nut, UriInfo uriInfo) {
        String protocol = uriInfo.getRequestUri().getScheme().equals("https") ? "sqrl" : "qrl";
        return UrlUtils
                .publicUriBuilder(uriInfo).scheme(protocol).replacePath("/bouncer.server/rest/auth/sqrl").queryParam("nut", nut).build().toString();
    }

    /**
     * Builds the qr's source url.
     *
     * @param srequest
     *            the srequest
     * @param nut
     *            the nut
     * @return the string
     */
    public static String buildQrUrl(HttpServletRequest srequest, String nut) {
        //"http://" + srequest.getLocalName() + ":" + srequest.getServerPort() +
        return  "/bouncer.server/rest/auth/sqrl-qr-code?nut=" + nut;
    }

    /**
     * Extract client parameter map from client request.
     *
     * @param req
     *            the req
     * @return the map
     */
    public static Map<String, String> extractClientParameterMap(HttpServletRequest req) {
        Map<String, String> map = new HashMap<>();
        map.put(SqrlClientRequestFieldNames.CLIENT, req.getParameter(SqrlClientRequestFieldNames.CLIENT));
        map.put(SqrlClientRequestFieldNames.SERVER, req.getParameter(SqrlClientRequestFieldNames.SERVER));
        map.put(SqrlClientRequestFieldNames.IDENTITY_SIGNATURE, req.getParameter(SqrlClientRequestFieldNames.IDENTITY_SIGNATURE));
        if (req.getParameter(SqrlClientRequestFieldNames.PREVIOUS_IDENTITY_SIGNATURE) != null) {
            map.put(SqrlClientRequestFieldNames.PREVIOUS_IDENTITY_SIGNATURE, req.getParameter(SqrlClientRequestFieldNames.PREVIOUS_IDENTITY_SIGNATURE));
        }
        if (req.getParameter(SqrlClientRequestFieldNames.UNLOCK_REQUEST_SIGNATURE) != null) {
            map.put(SqrlClientRequestFieldNames.UNLOCK_REQUEST_SIGNATURE, req.getParameter(SqrlClientRequestFieldNames.UNLOCK_REQUEST_SIGNATURE));
        }

        return map;
    }
}
