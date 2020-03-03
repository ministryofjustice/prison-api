package net.syscon.elite.security;

import net.syscon.elite.web.config.AuthAwareAuthenticationToken;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.Collections;

import static net.syscon.util.MdcUtility.PROXY_USER;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationFacadeTest {
    private final AuthenticationFacade authenticationFacade = new AuthenticationFacade();
    private static final OAuth2Request OAUTH_2_REQUEST = new OAuth2Request(Collections.emptyMap(), "client", Collections.emptySet(), true, Collections.emptySet(), Collections.emptySet(), "redirect", null, null);

    @Test
    public void isIdentifiedAuthentication_AuthSource_nomis() {
        setAuthentication("nomis", true);
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isTrue();
    }

    @Test
    public void isIdentifiedAuthentication_AuthSource_auth() {
        setAuthentication("auth", true);
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isFalse();
    }

    @Test
    public void isIdentifiedAuthentication_AuthSource_null() {
        setAuthentication(null, true);
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isTrue();
    }

    @Test
    public void isIdentifiedAuthentication_NoUserAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(OAUTH_2_REQUEST, null));
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isFalse();
    }

    @Test
    public void isIdentifiedAuthentication_AuthSource_nomis_no_proxy() {
        setAuthentication("nomis", false);
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isFalse();
    }

    @Test
    public void isIdentifiedAuthentication_AuthSource_auth_no_proxy() {
        setAuthentication("auth", false);
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isFalse();
    }

    @Test
    public void isIdentifiedAuthentication_AuthSource_null_no_proxy() {
        setAuthentication(null, false);
        assertThat(authenticationFacade.isIdentifiedAuthentication()).isFalse();
    }

    private void setAuthentication(final String source, boolean proxyUser) {
        final var userAuthentication = new AuthAwareAuthenticationToken("principal", "credentials", source, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(OAUTH_2_REQUEST, userAuthentication));
        if (proxyUser) {
            MDC.put(PROXY_USER, userAuthentication.getName());
        } else {
            MDC.remove(PROXY_USER);

        }
    }
}
