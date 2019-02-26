package net.syscon.elite.web.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AuthAwareAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final String authSource;

    public AuthAwareAuthenticationToken(final Object principal, final Object credentials, final String authSource, final Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.authSource = authSource != null ? authSource : "nomis";
    }

    public boolean isNomisSource() {
        return "nomis".equals(authSource);
    }
}
