package de.adorsys.smartlogin.sqrl;

import javax.enterprise.context.RequestScoped;
import java.io.Serializable;

/**
 * Store for client identity keys.
 * 
 * @author mko
 */
@RequestScoped
public class SqrlRequestKeyIdentityDepot implements Serializable{
    private static final long serialVersionUID = -6830287258052381750L;
    byte[] idk; // identity key
    byte[] pidk; // previous identity key
}
