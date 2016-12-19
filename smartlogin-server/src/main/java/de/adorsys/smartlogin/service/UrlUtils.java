package de.adorsys.smartlogin.service;


import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

public class UrlUtils {

    private static String publicBaseUrl() {
        return System.getenv("SQRL_PUBLIC_BASE_URL");
    }

    /**
     * Returns a {@link UriBuilder} that can be used to build external links into the application.
     */
    public static UriBuilder publicUriBuilder() {
        return UriBuilder.fromUri(publicBaseUrl());
    }
}
