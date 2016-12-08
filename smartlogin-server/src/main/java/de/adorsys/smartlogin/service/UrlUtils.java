package de.adorsys.smartlogin.service;


import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Utilities to build public urls to bouncer
 * <p/>
 *
 * @author Christoph Dietze
 * @see {@code de.adorsys.adscore.core.server.UrlUtils} does the analogous thing in adscore.
 */
public class UrlUtils {

    /**
     * Returns a {@link UriBuilder} that can be used to build external links into the application.
     * <p/>
     * If defined, the system property "bouncer.publicBaseUrl" is used as base, otherwise uriInfos is used as base.
     * The path of uriInfo is always appended to the result.
     * <p/>
     * Examples:
     * <table>
     * <tr><th>publicBaseUrl</th><th>uriInfo</th><th>result</th></tr>
     * <tr><td></td><td>http://internalname/rest/v1</td><td>http://internalname/rest/v1</td></tr>
     * <tr><td>https://integ:8080/</td><td>http://internalname/rest/v1</td><td>https://integ:8080/rest/v1</td></tr>
     * <tr><td>https://integ:8080/some/path</td><td>http://internalname/rest/v1</td><td>https://integ:8080/some/path/rest/v1</td></tr>
     * </table>
     */
    public static UriBuilder publicUriBuilder(UriInfo uriInfo) {
        return uriBuilder(uriInfo.getBaseUri());
    }

    private static String publicBaseUrl() {
        return System.getProperty("bouncer.publicBaseUrl");
    }

    private static UriBuilder uriBuilder(URI baseUriInfo) {
        return UriBuilder.fromUri(publicBaseUrl());
    }
}
