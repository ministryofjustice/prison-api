package net.syscon.elite.security.jwt;


import org.springframework.boot.context.properties.ConfigurationProperties;



@ConfigurationProperties(prefix = "security.jwt")
public class TokenSettings {

	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BASIC_AUTHENTICATION = "Basic";

	private String signingKey;
	private String schema;
	private int expiration;
	private String issuer;


	public String getSigningKey() {
		return signingKey;
	}

	public void setSigningKey(String signingKey) {
		this.signingKey = signingKey;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public int getExpiration() {
		return expiration;
	}

	public void setExpiration(int expiration) {
		this.expiration = expiration;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
}
