package net.syscon.elite.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.syscon.elite.web.api.model.Token;
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

	public Token createToken(final UserDetails userDetails) {
		if (StringUtils.isEmpty(userDetails.getUsername())) {
			throw new IllegalArgumentException("Cannot create JWT Token without username");
		}

		if (userDetails.getAuthorities() == null || userDetails.getAuthorities().isEmpty()) {
			throw new IllegalArgumentException("User doesn't have any privileges");
		}

		final Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
		claims.put("scopes", userDetails.getAuthorities().stream().map(s -> s.getAuthority()).collect(Collectors.toList()));

		final LocalDateTime now = LocalDateTime.now();

		final Date issuedAt = DateTimeConverter.toDate(now);
		final Date expiration = DateTimeConverter.toDate(now.plusMinutes(settings.getExpiration()));

		final Token token = new Token();

		final JwtBuilder builder = Jwts.builder()
				.setClaims(claims)
				.setIssuer(settings.getIssuer())
				.setId(UUID.randomUUID().toString())
				.setIssuedAt(issuedAt)
				.setExpiration(expiration)
				.signWith(SignatureAlgorithm.HS512, settings.getSigningKey());

		final String strToken = String.format("%s %s", settings.getSchema(), builder.compact());
		return new Token(strToken, issuedAt.getTime(), expiration.getTime());
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

	public Boolean validateToken(final String token, final UserDetails userDetails) {
		final Claims claims = this.getClaimsFromToken(token);
		final String username = claims.getSubject();
		boolean valid = true;
		if (!username.equals(userDetails.getUsername())) {
			valid = false;
		}
		Date expiration = claims.getExpiration();
		return valid && expiration.after(new Date());
	}

}


