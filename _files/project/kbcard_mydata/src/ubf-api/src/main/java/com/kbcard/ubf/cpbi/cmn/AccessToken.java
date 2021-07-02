package com.kbcard.ubf.cpbi.cmn;

public class AccessToken {
	/** 토큰일련번호 */
	private Long tokenSeq;
	/** access token */
	private String accessToken;
	/** access token 만료일시 */
	private String expYms;
	/** access token 범위구분코드 */
	private String tokenRngDtcd;
	/** access token 이용기관코드 */
	private String opnbUtzInsCd; 

	/** 이용기관 clientId */
	private String clientId;
	/** 이용기관 secret */
	private String clientSecret;

	private static AccessToken instance = new AccessToken();

	private AccessToken() {
	}

	public static synchronized AccessToken createInstance() {
		return instance;
	}

	public Long getTokenSeq() {
		return tokenSeq;
	}

	public void setTokenSeq(Long tokenSeq) {
		this.tokenSeq = tokenSeq;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getExpYms() {
		return expYms;
	}

	public void setExpYms(String expYms) {
		this.expYms = expYms;
	}

	public String getTokenRngDtcd() {
		return tokenRngDtcd;
	}

	public void setTokenRngDtcd(String tokenRngDtcd) {
		this.tokenRngDtcd = tokenRngDtcd;
	}

	public String getOpnbUtzInsCd() {
		return opnbUtzInsCd;
	}

	public void setOpnbUtzInsCd(String opnbUtzInsCd) {
		this.opnbUtzInsCd = opnbUtzInsCd;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}
