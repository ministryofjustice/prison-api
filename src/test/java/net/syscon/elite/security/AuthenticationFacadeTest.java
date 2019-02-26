package net.syscon.elite.security;

import net.syscon.elite.web.config.AuthAwareAuthenticationToken;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationFacadeTest {
    private final AuthenticationFacade authenticationFacade = new AuthenticationFacade();
    private static final OAuth2Request OAUTH_2_REQUEST = new OAuth2Request(Collections.emptyMap(), "client", Collections.emptySet(), true, Collections.emptySet(), Collections.emptySet(), "redirect", null, null);

    @Test
    public void isIdentifiedAuthentication_AuthSource_nomis() {
        setAuthentication("nomis");
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isTrue();
    }

    @Test
    public void isIdentifiedAuthentication_AuthSource_auth() {
        setAuthentication("auth");
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isFalse();
    }

    @Test
    public void isIdentifiedAuthentication_AuthSource_null() {
        setAuthentication(null);
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isTrue();
    }

    @Test
    public void isIdentifiedAuthentication_NoUserAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(OAUTH_2_REQUEST, null));
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isFalse();
    }

    private void setAuthentication(final String source) {
        final var userAuthentication = new AuthAwareAuthenticationToken("principal", "credentials", source, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(OAUTH_2_REQUEST, userAuthentication));
    }
}
