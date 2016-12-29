package de.adorsys.smartlogin.spi;

/**
 * Provides connectivity to an identity provider.
 * 
 * @author fpo
 *
 */
public interface IdpAccountProvider {

	boolean idpAccountExists(String userLogin);

}
