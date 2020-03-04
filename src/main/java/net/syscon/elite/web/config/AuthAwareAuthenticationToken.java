package net.syscon.elite.web.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

@Getter
public class AuthAwareAuthenticationToken extends JwtAuthenticationToken {
    private final String authSource;
    private final Object principal;

    public AuthAwareAuthenticationToken(final Jwt jwt, final Object principal, final String authSource, final Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.authSource = authSource != null ? authSource : "nomis";
        this.principal = principal;
    }

    public boolean isNomisSource() {
        return "nomis".equals(authSource);
    }
}
