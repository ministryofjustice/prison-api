package uk.gov.justice.hmpps.prison.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.justice.hmpps.prison.web.config.AuthAwareAuthenticationToken;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.hmpps.prison.security.AuthSource.AUTH;
import static uk.gov.justice.hmpps.prison.security.AuthSource.NOMIS;
import static uk.gov.justice.hmpps.prison.security.AuthSource.NONE;

public class AuthenticationFacadeTest {
    private final AuthenticationFacade authenticationFacade = new AuthenticationFacade();

    @Test
    public void getAuthenticationSource_AuthSource_nomis() {
        setAuthentication("nomis");
        assertThat(authenticationFacade.getAuthenticationSource()).isEqualTo(NOMIS);
    }

    @Test
    public void getProxyUserAuthenticationSource_AuthSource_auth() {
        setAuthentication("auth");
        assertThat(authenticationFacade.getAuthenticationSource()).isEqualTo(AUTH);
    }

    @Test
    public void getProxyUserAuthenticationSource_AuthSource_null() {
        setAuthentication(null);
        assertThat(authenticationFacade.getAuthenticationSource()).isEqualTo(NONE);
    }

    @Test
    public void getProxyUserAuthenticationSource_NoUserAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
        assertThat(authenticationFacade.getAuthenticationSource()).isEqualTo(NONE);
    }

    @ParameterizedTest
    @CsvSource({
        "ROLE_SYSTEM_USER,true",
        "SYSTEM_USER,true",
        "SYSTEMUSER,false"
    })
    public void hasRolesTest(String role, boolean expected) {
        setAuthentication("auth", Set.of(
            new SimpleGrantedAuthority("ROLE_SYSTEM_USER")
        ));

        assertThat(authenticationFacade.hasRoles(role)).isEqualTo(expected);
        assertThat(authenticationFacade.isOverrideRole(role)).isEqualTo(expected);
    }

    private void setAuthentication(final String source) {
        setAuthentication(source, emptySet());
    }

    private void setAuthentication(final String source, Set<GrantedAuthority> authoritySet) {
        final Authentication auth = new AuthAwareAuthenticationToken(mock(Jwt.class), "client", source, authoritySet);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
