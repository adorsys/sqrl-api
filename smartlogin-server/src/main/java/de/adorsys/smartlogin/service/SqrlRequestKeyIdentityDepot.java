package de.adorsys.smartlogin.service;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;

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
