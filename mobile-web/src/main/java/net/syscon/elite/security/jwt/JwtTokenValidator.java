package net.syscon.elite.security.jwt;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import net.syscon.elite.security.EliteUser;

@Component
public class JwtTokenValidator {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Value("${jwt.secret}")
    private String secret;

    @SuppressWarnings("unchecked")
	public EliteUser parseToken(final String token) {
        try {
            final Claims body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            
            return new EliteUser(body.getSubject(), token, (List<GrantedAuthority>)body.get("roles"));
        } catch (final JwtException e) {
        	throw new JwtTokenMalformedException(e.getMessage(), e);
        }
    }
}