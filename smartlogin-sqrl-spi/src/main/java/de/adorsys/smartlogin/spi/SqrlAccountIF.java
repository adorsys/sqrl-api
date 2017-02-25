package de.adorsys.smartlogin.spi;

/**
 * Account API for SQRL.
 * 
 * @author fpo
 *
 */
public interface SqrlAccountIF {

    public String getIdpAccountId();

    public byte[] getSqrlIdentityKey();

    public byte[] getSqrlServerUnlockKey();

    public byte[] getSqrlVerifyUnlockKey();

}
