package net.syscon.elite.security.jwt;


import java.time.LocalDateTime;
import java.util.Date;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.syscon.elite.security.DeviceFingerprint;
import net.syscon.elite.web.api.model.Token;
import net.syscon.util.DateTimeConverter;

public class TokenManagement {

	private static final String SCOPES = "scopes";
	private static final String DEVICE_FINGERPRINT_HASH_CODE = "deviceFingerprintHashCode";
	private static final String ALLOW_REFRESH_TOKEN = "allowRefreshToken";
	private TokenSettings settings;

	@Inject
	public void setTokenSettins(final TokenSettings settings) {
		this.settings = settings;
	}

	public Token createToken(final String username) {
		final String usernameToken = username.toUpperCase();
		final Claims claims = Jwts.claims().setSubject(usernameToken);
		final int deviceFingerprintHashCode = DeviceFingerprint.get().hashCode();
		claims.put(DEVICE_FINGERPRINT_HASH_CODE, deviceFingerprintHashCode);
		claims.put(ALLOW_REFRESH_TOKEN, Boolean.FALSE);

		final LocalDateTime now = LocalDateTime.now();

		final Date issuedAt = DateTimeConverter.toDate(now);
		final Date expiration = DateTimeConverter.toDate(now.plusMinutes(settings.getExpiration()));
		final Date refreshExpiration = DateTimeConverter.toDate(now.plusMinutes(settings.getRefreshExpiration()));

		final Token token = new Token();

		final JwtBuilder builder = Jwts.builder()
				.setClaims(claims)
				.setIssuer(settings.getIssuer())
				.setIssuedAt(issuedAt)
				.setExpiration(expiration)
				.signWith(SignatureAlgorithm.HS512, settings.getSigningKey());

		final Claims refreshClaims = Jwts.claims().setSubject(usernameToken);
		refreshClaims.put(DEVICE_FINGERPRINT_HASH_CODE, deviceFingerprintHashCode);
		refreshClaims.put(ALLOW_REFRESH_TOKEN, Boolean.TRUE);

		final JwtBuilder refreshBuilder = Jwts.builder()
				.setClaims(refreshClaims)
				.setIssuer(settings.getIssuer())
				.setIssuedAt(issuedAt)
				.setExpiration(refreshExpiration)
				.signWith(SignatureAlgorithm.HS512, settings.getSigningKey());

		final String strToken = String.format("%s %s", settings.getSchema(), builder.compact());
		final String strRefreshToken = String.format("%s %s", settings.getSchema(), refreshBuilder.compact());

		token.setToken(strToken);
		token.setIssuedAt(issuedAt.getTime());
		token.setExpiration(expiration.getTime());
		token.setRefreshToken(strRefreshToken);
		token.setRefreshExpiration(refreshExpiration.getTime());
		return token;
	}


	private Claims getClaimsFromToken(final String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(settings.getSigningKey()).parseClaimsJws(token).getBody();
		} catch (final Exception e) {
			claims = null;
		}
		return claims;
	}

	public String getUsernameFromToken(final String token) {
		String username;
		try {
			final Claims claims = getClaimsFromToken(token);
			username = claims.getSubject();
		} catch (final Exception e) {
			username = null;
		}
		return username;
	}

	public Boolean validateToken(final String token, final UserDetails userDetails, final DeviceFingerprint deviceFingerprint, final boolean refreshingToken) {
		final Claims claims = this.getClaimsFromToken(token);
		final String username = claims.getSubject();
		final Boolean allowRefreshToken = (Boolean) claims.get(ALLOW_REFRESH_TOKEN);
		boolean valid = token != null && userDetails != null && deviceFingerprint != null && username != null;

		// validate the kind of token
		if (refreshingToken && !allowRefreshToken || !refreshingToken && allowRefreshToken) {
			valid = false;
		}

		if (valid && !username.equals(userDetails.getUsername())) {
			valid = false;
		}

		final Integer deviceFingerprintHashCode = (Integer) claims.get(DEVICE_FINGERPRINT_HASH_CODE);
		if (valid && deviceFingerprint != null && deviceFingerprintHashCode != null) {
			valid = deviceFingerprint.hashCode() == deviceFingerprintHashCode.intValue();
		}

		final Date expiration = claims.getExpiration();
		if (valid && expiration != null) {
			valid =  expiration.after(new Date());
		}

		return valid;
	}

}


