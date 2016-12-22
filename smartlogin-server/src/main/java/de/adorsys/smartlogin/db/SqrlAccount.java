package de.adorsys.smartlogin.db;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by alexg on 22.12.16.
 */
@Entity(value = "account", noClassnameStored = true)
@Indexes({@Index(fields = @Field(value = SqrlAccount.Fields.ACCOUNT_SQRL_KEY_IDENTITY)) })
public class SqrlAccount {

    public interface Fields {
        String ID = "_id";
        String ACCOUNT_ALIAS = "accountAlias";
        String ACCOUNT_SQRL_KEY_IDENTITY = "sqrlIdentityKey";
        String ACCOUNT_SQRL_KEY_SERVER_UNLOCK = "sqrlServerUnlockKey";
        String ACCOUNT_SQRL_KEY_VERIFY_UNLOCK = "sqrlVerifyUnlockKey";
    }

    @Id
    private ObjectId accountId;
    private String accountAlias;
    private byte[] sqrlIdentityKey;
    private byte[] sqrlServerUnlockKey;
    private byte[] sqrlVerifyUnlockKey;

    public ObjectId getAccountId() {
        return accountId;
    }

    public SqrlAccount accountId(ObjectId accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getAccountAlias() {
        return accountAlias;
    }

    public SqrlAccount accountAlias(String accountAlias) {
        this.accountAlias = accountAlias;
        return this;
    }

    public byte[] getSqrlIdentityKey() {
        return sqrlIdentityKey;
    }

    public SqrlAccount sqrlIdentityKey(byte[] sqrlIdentityKey) {
        this.sqrlIdentityKey = sqrlIdentityKey;
        return this;
    }

    public byte[] getSqrlServerUnlockKey() {
        return sqrlServerUnlockKey;
    }

    public SqrlAccount sqrlServerUnlockKey(byte[] sqrlServerUnlockKey) {
        this.sqrlServerUnlockKey = sqrlServerUnlockKey;
        return this;
    }

    public byte[] getSqrlVerifyUnlockKey() {
        return sqrlVerifyUnlockKey;
    }

    public SqrlAccount sqrlVerifyUnlockKey(byte[] sqrlVerifyUnlockKey) {
        this.sqrlVerifyUnlockKey = sqrlVerifyUnlockKey;
        return this;
    }
}
