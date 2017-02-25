package de.adorsys.smartlogin.spi;

/**
 * Created by alexg on 07.12.16.
 */
public interface SqrlAccountProvider {
    
    byte[] fetchServerUnlockKey(byte[] idk);

    byte[] fetchVerifyUnlockKey(byte[] idk);

    boolean insertSqrlKeys(String userLogin, byte[] idk, byte[] serverUnlockKey, byte[] verifyUnlockKey);

    boolean updateSqrlKeys(byte[] previousIdentityKey, byte[] idk, byte[] serverUnlockKey, byte[] verifyUnlockKey);

    boolean updateSqrlServerAndVerifyUnlockKey(byte[] idk, byte[] serverUnlockKey, byte[] verifyUnlockKey);

    String checkIdentity(byte[] identityKey);

    void createSqrlAccount(String idpAccountId);

    boolean accountExistsBySqrlAccountId(String id);

    boolean accountExistsByIdpAccountId(String id);

    boolean sqrlIdentityExists(String userId);

    void deleteSqrlIdentityIfExists(String userId);

	String getIdpAccountId(String accountId);
}
