package de.adorsys.smartlogin.sqrl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Own exception to ease use - wraps WebApplicationException.
 */
public class SqrlAuthException extends WebApplicationException {

    private static final long serialVersionUID = 1L;

    public SqrlAuthException(Response.Status status) {
        super(status);
    }

    public SqrlAuthException(Response response) {
        super(response);
    }

}
