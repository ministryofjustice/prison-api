package uk.gov.justice.hmpps.prison.security;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.justice.hmpps.prison.web.config.AuthAwareAuthenticationToken;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.hmpps.prison.security.AuthSource.AUTH;
import static uk.gov.justice.hmpps.prison.security.AuthSource.NOMIS;
import static uk.gov.justice.hmpps.prison.security.AuthSource.NONE;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.PROXY_USER;

public class AuthenticationFacadeTest {
    private final AuthenticationFacade authenticationFacade = new AuthenticationFacade();

    @Test
    public void getProxyUserAuthenticationSource_AuthSource_nomis() {
        setAuthentication("nomis", true);
        assertThat(authenticationFacade.getProxyUserAuthenticationSource()).isEqualTo(NOMIS);
    }

    @Test
    public void getProxyUserAuthenticationSource_AuthSource_auth() {
        setAuthentication("auth", true);
        assertThat(authenticationFacade.getProxyUserAuthenticationSource()).isEqualTo(AUTH);
    }

    @Test
    public void getProxyUserAuthenticationSource_AuthSource_null() {
        setAuthentication(null, true);
        assertThat(authenticationFacade.getProxyUserAuthenticationSource()).isEqualTo(NONE);
    }

    @Test
    public void getProxyUserAuthenticationSource_NoUserAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
        assertThat(authenticationFacade.getProxyUserAuthenticationSource()).isEqualTo(NONE);
    }

    @Test
    public void getProxyUserAuthenticationSource_AuthSource_nomis_no_proxy() {
        setAuthentication("nomis", false);
        assertThat(authenticationFacade.getProxyUserAuthenticationSource()).isEqualTo(NONE);
    }

    @Test
    public void getProxyUserAuthenticationSource_AuthSource_auth_no_proxy() {
        setAuthentication("auth", false);
        assertThat(authenticationFacade.getProxyUserAuthenticationSource()).isEqualTo(NONE);
    }

    @Test
    public void getProxyUserAuthenticationSource_AuthSource_null_no_proxy() {
        setAuthentication(null, false);
        assertThat(authenticationFacade.getProxyUserAuthenticationSource()).isEqualTo(NONE);
    }

    private void setAuthentication(final String source, boolean proxyUser) {
        final Authentication auth = new AuthAwareAuthenticationToken(mock(Jwt.class), "client", source, emptySet());
        SecurityContextHolder.getContext().setAuthentication(auth);
        if (proxyUser) {
            MDC.put(PROXY_USER, "client");
        } else {
            MDC.remove(PROXY_USER);

        }
    }
}
