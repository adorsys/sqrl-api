package de.adorsys.smartlogin.provider;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Created by alexg on 19.12.16.
 */
@ApplicationScoped
public class AccountProviderImpl implements AccountProvider{

    public String checkIdentity(byte[] identityKey) {
        return UUID.randomUUID().toString();
    }

    public boolean accountExists(String accountId) {
        return true;
    }
    public boolean sqrlIdentityExists(String accountId) {
        return true;
    }
    public void deleteSqrlIdentityIfExists(String accountId) {
    }

    public byte[] fetchServerUnlockKey(byte[] identityKey) {
        return UUID.randomUUID().toString().getBytes();
    }
    public byte[] fetchVerifyUnlockKey(byte[] identityKey) {
        return UUID.randomUUID().toString().getBytes();
    }

    public boolean insertSqrlKeys(String userLogin, byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
       return true;
    }
    public boolean updateSqrlKeys(byte[] previousIdentityKey, byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        return true;
    }
    public boolean updateSqrlServerAndVerifyUnlockKey(byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        return true;
    }
}
