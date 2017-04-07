package net.syscon.elite.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.syscon.util.DeviceProvider;


public class TokenManager {


	private final String AUDIENCE_UNKNOWN = "unknown";
	private final String AUDIENCE_WEB = "web";
	private final String AUDIENCE_MOBILE = "mobile";
	private final String AUDIENCE_TABLET = "tablet";

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;
	
	@Inject
	private DeviceProvider deviceProvider;
	

	public String getUsernameFromToken(final String token) {
		String username;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			username = claims.getSubject();
		} catch (final Exception e) {
			username = null;
		}
		return username;
	}

	public Date getCreatedDateFromToken(final String token) {
		Date created;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			created = new Date((Long) claims.get("created"));
		} catch (final Exception e) {
			created = null;
		}
		return created;
	}

	public Date getExpirationDateFromToken(final String token) {
		Date expiration;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			expiration = claims.getExpiration();
		} catch (final Exception e) {
			expiration = null;
		}
		return expiration;
	}

	public String getAudienceFromToken(final String token) {
		String audience;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			audience = (String) claims.get("audience");
		} catch (final Exception e) {
			audience = null;
		}
		return audience;
	}

	private Claims getClaimsFromToken(final String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();
		} catch (final Exception e) {
			claims = null;
		}
		return claims;
	}

	private Date generateCurrentDate() {
		return new Date(System.currentTimeMillis());
	}

	private Date generateExpirationDate() {
		return new Date(System.currentTimeMillis() + this.expiration * 1000);
	}

	private Boolean isTokenExpired(final String token) {
		final Date expiration = this.getExpirationDateFromToken(token);
		return expiration.before(this.generateCurrentDate());
	}

	private Boolean isCreatedBeforeLastPasswordReset(final Date created, final Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}

	private String generateAudience(final Device device) {
		String audience = this.AUDIENCE_UNKNOWN;
		if (device != null) {
			if (device.isNormal()) {
				audience = this.AUDIENCE_WEB;
			} else if (device.isTablet()) {
				audience = AUDIENCE_TABLET;
			} else if (device.isMobile()) {
				audience = AUDIENCE_MOBILE;
			}
		}
		return audience;
	}

	private Boolean ignoreTokenExpiration(final String token) {
		final String audience = this.getAudienceFromToken(token);
		return (this.AUDIENCE_TABLET.equals(audience) || this.AUDIENCE_MOBILE.equals(audience));
	}

	public String generateToken(final UserDetails userDetails) {
		final Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("sub", userDetails.getUsername());
		claims.put("audience", this.generateAudience(deviceProvider.get()));
		claims.put("created", this.generateCurrentDate());
		return this.generateToken(claims);
	}

	private String generateToken(final Map<String, Object> claims) {
		return Jwts.builder().setClaims(claims).setExpiration(this.generateExpirationDate()).signWith(SignatureAlgorithm.HS512, this.secret).compact();
	}

	public Boolean canTokenBeRefreshed(final String token, final Date lastPasswordReset) {
		final Date created = this.getCreatedDateFromToken(token);
		return (!(this.isCreatedBeforeLastPasswordReset(created, lastPasswordReset)) && (!(this.isTokenExpired(token)) || this.ignoreTokenExpiration(token)));
	}

	public String refreshToken(final String token) {
		String refreshedToken;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			claims.put("created", this.generateCurrentDate());
			refreshedToken = this.generateToken(claims);
		} catch (final Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}

	public Boolean validateToken(final String token, final UserDetails userDetails) {
		final String username = this.getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !(this.isTokenExpired(token)));
	}

}
