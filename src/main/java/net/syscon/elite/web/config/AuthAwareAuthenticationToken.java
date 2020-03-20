package net.syscon.elite.web.config;

import lombok.Getter;
import net.syscon.elite.security.AuthSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

import static net.syscon.elite.security.AuthSource.NOMIS;

@Getter
public class AuthAwareAuthenticationToken extends JwtAuthenticationToken {
    private final AuthSource authSource;
    private final Object principal;

    public AuthAwareAuthenticationToken(final Jwt jwt, final Object principal, final String authSource, final Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.authSource = AuthSource.fromName(authSource);
        this.principal = principal;
    }

    public boolean isNomisSource() {
        return NOMIS.equals(authSource);
    }
}
