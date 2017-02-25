package de.adorsys.smartlogin.db;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(indexes={@Index(unique=true,columnList="sqrlIdentityKey")})
public class SqrlAccount {

    @Id
    private String accountId;
    private String idpAccountId;
    private byte[] sqrlIdentityKey;
    private byte[] sqrlServerUnlockKey;
    private byte[] sqrlVerifyUnlockKey;
        
    @PrePersist
    public void postConstruct(){
    	if(accountId==null)
    		accountId = UUID.randomUUID().toString();
    }
    
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getIdpAccountId() {
		return idpAccountId;
	}
	public void setIdpAccountId(String idpAccountId) {
		this.idpAccountId = idpAccountId;
	}

	public byte[] getSqrlIdentityKey() {
		return sqrlIdentityKey;
	}

	public void setSqrlIdentityKey(byte[] sqrlIdentityKey) {
		this.sqrlIdentityKey = sqrlIdentityKey;
	}

	public byte[] getSqrlServerUnlockKey() {
		return sqrlServerUnlockKey;
	}

	public void setSqrlServerUnlockKey(byte[] sqrlServerUnlockKey) {
		this.sqrlServerUnlockKey = sqrlServerUnlockKey;
	}

	public byte[] getSqrlVerifyUnlockKey() {
		return sqrlVerifyUnlockKey;
	}

	public void setSqrlVerifyUnlockKey(byte[] sqrlVerifyUnlockKey) {
		this.sqrlVerifyUnlockKey = sqrlVerifyUnlockKey;
	}
}
