package uk.gov.justice.hmpps.prison.web.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uk.gov.justice.hmpps.prison.security.AuthSource;

import java.util.Collection;

@Getter
public class AuthAwareAuthenticationToken extends JwtAuthenticationToken {
    private final AuthSource authSource;
    private final Object principal;
    private final Object clientId;
    private final Object grantType;
    private final boolean clientOnly;

    public AuthAwareAuthenticationToken(final Jwt jwt, final Object principal, final Object clientId, final Object grantType, final boolean clientOnly, final String authSource, final Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.authSource = AuthSource.fromName(authSource);
        this.clientOnly = clientOnly;
        this.principal = principal;
        this.clientId = clientId;
        this.grantType = grantType;
    }
}
