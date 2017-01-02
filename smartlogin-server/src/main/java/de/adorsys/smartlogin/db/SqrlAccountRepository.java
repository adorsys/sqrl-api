package de.adorsys.smartlogin.db;

import de.adorsys.smartlogin.spi.SqrlAccountProvider;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by alexg on 22.12.16.
 */
@ApplicationScoped
public class SqrlAccountRepository implements SqrlAccountProvider {

    @Inject
    private Datastore datastore;

    public void createSqrlAccount(SqrlAccount sqrlAccount) {
        datastore.save(sqrlAccount);
    }

    public boolean accountExistsBySqrlAccountId(String accountId) {
        ObjectId id = new ObjectId(accountId);
        return  datastore.createQuery(SqrlAccount.class).field(SqrlAccount.Fields.ID).equal(id).countAll() > 0;
    }

    @Override
    public boolean accountExistsByIdpAccountId(String id) {
        return  datastore.createQuery(SqrlAccount.class).field(SqrlAccount.Fields.IDP_ACCOUNT_ID).equal(id).countAll() > 0;
    }

    public String checkIdentity(byte[] identityKey) {
        Query<SqrlAccount> query = datastore.createQuery(SqrlAccount.class);
        query.criteria(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_IDENTITY).equal(identityKey);

        SqrlAccount account = query.get();
        if (account == null) {
            return null;
        }

        return account.getAccountId().toString();
    }

    public boolean sqrlIdentityExists(String accountId) {
        ObjectId id = new ObjectId(accountId);
        Query<SqrlAccount> query = datastore.createQuery(SqrlAccount.class).field(SqrlAccount.Fields.ID).equal(id);
        SqrlAccount p = query.retrievedFields(true, SqrlAccount.Fields.ACCOUNT_SQRL_KEY_IDENTITY).get();
        return (p != null && p.getSqrlIdentityKey() != null);
    }

    public void deleteSqrlIdentityIfExists(String accountId) {
        ObjectId accountIdObject = new ObjectId(accountId);
        Query<SqrlAccount> query = datastore.createQuery(SqrlAccount.class).field(SqrlAccount.Fields.ID).equal(accountIdObject);

        this.doUpdateSqrlKeys(query.get(), null, null, null);
    }

    public byte[] fetchServerUnlockKey(byte[] identityKey) {
        SqrlAccount result = fetchSqrlAccountWithKeyByFieldName(identityKey, SqrlAccount.Fields.ACCOUNT_SQRL_KEY_SERVER_UNLOCK);
        if (result == null) {
            return null;
        }
        return result.getSqrlServerUnlockKey();

    }

    public byte[] fetchVerifyUnlockKey(byte[] identityKey) {
        SqrlAccount result = fetchSqrlAccountWithKeyByFieldName(identityKey, SqrlAccount.Fields.ACCOUNT_SQRL_KEY_VERIFY_UNLOCK);
        if (result == null) {
            return null;
        }
        return result.getSqrlVerifyUnlockKey();
    }

    // internal key fetching helper //

    private SqrlAccount fetchSqrlAccountWithKeyByFieldName(byte[] identityKey, String fieldName) {
        Query<SqrlAccount> query = datastore.createQuery(SqrlAccount.class);
        query.criteria(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_IDENTITY).equal(identityKey);
        SqrlAccount result = query.retrievedFields(true, fieldName).get();
        return result;
    }

    /** sqrl key manipulation **/

    public boolean insertSqrlKeys(String userLogin, byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        Query<SqrlAccount> query = datastore.createQuery(SqrlAccount.class);
        query.criteria(SqrlAccount.Fields.IDP_ACCOUNT_ID).equal(userLogin);
        return doUpdateSqrlKeys(query.get(), identityKey, serverUnlockKey, verifyUnlockKey);
    }

    public boolean updateSqrlKeys(byte[] previousIdentityKey, byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        Query<SqrlAccount> query = datastore.createQuery(SqrlAccount.class);
        query.criteria(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_IDENTITY).equal(previousIdentityKey);
        SqrlAccount result = query.get();
        if (result == null) {
            return false;
        }
        return doUpdateSqrlKeys(result, identityKey, serverUnlockKey, verifyUnlockKey);
    }

    public boolean updateSqrlServerAndVerifyUnlockKey(byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        Query<SqrlAccount> query = datastore.createQuery(SqrlAccount.class);
        query.criteria(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_IDENTITY).equal(identityKey);

        return doUpdateSqrlKeys(query.get(), identityKey, serverUnlockKey, verifyUnlockKey);
    }

    // internal key manipulation helper //

    /**
     * Update sqrl in db.
     *
     * @param target          the target
     * @param identityKey     the identity key
     * @param serverUnlockKey the server unlock key
     * @param verifyUnlockKey the verify unlock key
     * @return true, if successful
     */
    private boolean doUpdateSqrlKeys(SqrlAccount target, byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        UpdateOperations<SqrlAccount> ops = datastore.createUpdateOperations(SqrlAccount.class);
        if (identityKey == null) {
            ops.unset(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_IDENTITY);
        } else {
            ops.set(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_IDENTITY, identityKey);
        }

        if (serverUnlockKey == null) {
            ops.unset(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_SERVER_UNLOCK);
        } else {
            ops.set(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_SERVER_UNLOCK, serverUnlockKey);
        }

        if (verifyUnlockKey == null) {
            ops.unset(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_VERIFY_UNLOCK);
        } else {
            ops.set(SqrlAccount.Fields.ACCOUNT_SQRL_KEY_VERIFY_UNLOCK, verifyUnlockKey);
        }

        datastore.update(target, ops);
        return true;
    }
    
    @Override
    public String getIdpAccountId(String accountId) {
        Query<SqrlAccount> query = datastore.createQuery(SqrlAccount.class);
        query.criteria(SqrlAccount.Fields.ID).equal(new ObjectId(accountId));
        query.field(SqrlAccount.Fields.IDP_ACCOUNT_ID);
        SqrlAccount account = query.get();
        if (account == null) {
            return null;
        }

        return account.getIdpAccountId();
    }
    

}
