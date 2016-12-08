package de.adorsys.smartlogin.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Response type to deliver data for a successful authentication.
 * 
 * @author mko
 */
public class SqrlResponse {

	public static interface Fields{
		String ACCESS_TOKEN_ID = "accessTokenId";
		String EXPIRATION_DURATION = "expirationDuration";
	}
	
    private String accessTokenId;
    private long expirationDuration;
    
    public SqrlResponse() {
    }
    
    public SqrlResponse(String accessToken, long expirationDuration) {
        super();
        this.accessTokenId = accessToken;
        this.expirationDuration = expirationDuration;
    }

    public String getAccessToken() {
        return accessTokenId;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessTokenId = accessToken;
    }
    
    public long getExpirationDuration() {
        return expirationDuration;
    }
    
    public void setExpirationDuration(long expirationDuration) {
        this.expirationDuration = expirationDuration;
    }

	public Map<String, String> asMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(SqrlResponse.Fields.ACCESS_TOKEN_ID, getAccessToken());
		map.put(SqrlResponse.Fields.EXPIRATION_DURATION, "" + getExpirationDuration());
		return map;
	}
    
}
