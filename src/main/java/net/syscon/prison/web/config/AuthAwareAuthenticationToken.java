package net.syscon.prison.web.config;

import lombok.Getter;
import net.syscon.prison.security.AuthSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

@Getter
public class AuthAwareAuthenticationToken extends JwtAuthenticationToken {
    private final AuthSource authSource;
    private final Object principal;

    public AuthAwareAuthenticationToken(final Jwt jwt, final Object principal, final String authSource, final Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.authSource = AuthSource.fromName(authSource);
        this.principal = principal;
    }
}
