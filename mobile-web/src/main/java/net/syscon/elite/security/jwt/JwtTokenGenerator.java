package net.syscon.elite.security.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.syscon.elite.security.EliteUser;

/**
 * convenience class to generate a token for testing your requests.
 * Make sure the used secret here matches the on in your application.yml
 *
 * @author pascal alma
 */
public class JwtTokenGenerator {

    public static String generateToken(final EliteUser u, final String secret) {
        final Claims claims = Jwts.claims().setSubject(u.getUsername());
        claims.put("roles", u.getAuthorities());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    public static void main(final String[] args) {
        final SimpleGrantedAuthority adminRole = new SimpleGrantedAuthority("ADMIN");
        final EliteUser user = new EliteUser("wellington", null, adminRole);
        System.out.println("**************************************\n\n" + generateToken(user, "my-very-secret-key") + "\n\n");
    }
}