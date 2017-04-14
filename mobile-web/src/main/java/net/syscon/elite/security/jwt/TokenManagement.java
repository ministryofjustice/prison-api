package net.syscon.elite.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.syscon.util.DateTimeConverter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

public class TokenManagement {


	private TokenSettings settings;

	@Inject
	public void setTokenSettins(final TokenSettings settings) {
		this.settings = settings;
	}

	public String createToken(final UserDetails userDetails, boolean isRefresh) {
		if (StringUtils.isEmpty(userDetails.getUsername())) {
			throw new IllegalArgumentException("Cannot create JWT Token without username");
		}

		if (userDetails.getAuthorities() == null || userDetails.getAuthorities().isEmpty()) {
			throw new IllegalArgumentException("User doesn't have any privileges");
		}

		final Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
		claims.put("scopes", userDetails.getAuthorities().stream().map(s -> s.getAuthority()).collect(Collectors.toList()));

		final LocalDateTime now = LocalDateTime.now();
		int expiration = isRefresh? settings.getExpiration(): settings.getRefreshExpiration();


		final JwtBuilder builder = Jwts.builder()
				.setClaims(claims)
				.setIssuer(settings.getIssuer())
				.setIssuedAt(DateTimeConverter.toDate(now))
				.setExpiration(DateTimeConverter.toDate(now.plusMinutes(expiration)))
				.signWith(SignatureAlgorithm.HS512, settings.getSigningKey());

		if (isRefresh) {
			builder.setId(UUID.randomUUID().toString());
		}
		return builder.compact();

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

	public Boolean validateToken(final String token, final UserDetails userDetails, boolean isAutentication) {
		final Claims claims = this.getClaimsFromToken(token);
		final String username = claims.getSubject();
		boolean valid = true;
		if (!username.equals(userDetails.getUsername())) {
			valid = false;
		}
		if (valid && !isAutentication && claims.containsKey("id")) {
			valid = false;
		}
		Date expiration = claims.getExpiration();
		if (valid && expiration.after(new Date())) {
			valid = false;
		}
		return valid;
	}

}


