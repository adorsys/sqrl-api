package de.adorsys.smartlogin.directgrant.sqrl;

public class SqrlLoginCredentials {

	private String accessTokenId;
	
	private String nut;

	public String getAccessTokenId() {
		return accessTokenId;
	}

	public void setAccessTokenId(String accessTokenId) {
		this.accessTokenId = accessTokenId;
	}

	public String getNut() {
		return nut;
	}

	public void setNut(String nut) {
		this.nut = nut;
	}

	@Override
	public String toString() {
		return "SqrlLoginCredentials [accessTokenId=" + accessTokenId + ", nut=" + nut + "]";
	}
	
}
