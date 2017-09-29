package net.syscon.elite.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.syscon.elite.api.model.Token;
import net.syscon.elite.security.DeviceFingerprint;
import net.syscon.elite.security.UserDetailsImpl;
import net.syscon.util.DateTimeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;


public class TokenManagement {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final String DEVICE_FINGERPRINT_HASH_CODE = "deviceFingerprintHashCode";
	private static final String ALLOW_REFRESH_TOKEN = "allowRefreshToken";
	private static final String USER_PRINCIPAL = "userPrincipal";

	private final TokenSettings settings;
	private final boolean upperCaseUsername;

	public TokenManagement(TokenSettings settings, 	@Value("${token.username.stored.caps:true}") boolean upperCaseUsername) {
		this.settings = settings;
		this.upperCaseUsername = upperCaseUsername;
	}

	public Token createToken(String username) {
		UserDetailsImpl userDetails = new UserDetailsImpl(username, null, Collections.emptyList(), null);
		return createToken(userDetails);
	}

	public Token createToken(UserDetailsImpl userDetails) {
		final String usernameToken = upperCaseUsername ? userDetails.getUsername().toUpperCase() : userDetails.getUsername();
		final Claims claims = Jwts.claims().setSubject(usernameToken);
		final int deviceFingerprintHashCode = DeviceFingerprint.get().hashCode();

		claims.put(DEVICE_FINGERPRINT_HASH_CODE, deviceFingerprintHashCode);
		claims.put(ALLOW_REFRESH_TOKEN, Boolean.FALSE);
		claims.put(USER_PRINCIPAL, userDetails);

		final LocalDateTime now = LocalDateTime.now();

		log.info("Token expirection is {} mins, refresh expire is {} mins", settings.getExpiration(), settings.getRefreshExpiration());
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
		refreshClaims.put(USER_PRINCIPAL, userDetails);

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

	public Object getUserPrincipalFromToken(String token) {
		Object userPrincipal;

		try {
			Claims claims = getClaimsFromToken(token);

			userPrincipal = claims.get(USER_PRINCIPAL);
		} catch (Exception ex) {
			userPrincipal = null;
		}

		return userPrincipal;
	}

	Boolean validateToken(String token, Object userPrincipal, DeviceFingerprint deviceFingerprint, boolean refreshingToken) {
		final Claims claims = this.getClaimsFromToken(token);
		final Boolean allowRefreshToken = (Boolean) claims.get(ALLOW_REFRESH_TOKEN);

		boolean valid = token != null && deviceFingerprint != null && userPrincipal != null;

		// validate the kind of token
		if (refreshingToken && !allowRefreshToken || !refreshingToken && allowRefreshToken) {
			valid = false;
		}

		final Integer deviceFingerprintHashCode = (Integer) claims.get(DEVICE_FINGERPRINT_HASH_CODE);

		if (valid && deviceFingerprintHashCode != null) {
			valid = deviceFingerprint.hashCode() == deviceFingerprintHashCode;
		}

		final Date expiration = claims.getExpiration();

		if (valid && expiration != null) {
			valid =  expiration.after(new Date());
		}

		return valid;
	}

	private Claims getClaimsFromToken(String token) {
		Claims claims;

		try {
			claims = Jwts.parser().setSigningKey(settings.getSigningKey()).parseClaimsJws(token).getBody();
		} catch (final Exception e) {
			claims = null;
		}

		return claims;
	}
}
