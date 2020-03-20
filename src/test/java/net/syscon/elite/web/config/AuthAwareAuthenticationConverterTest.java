package net.syscon.elite.web.config;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.syscon.elite.security.AuthSource.AUTH;
import static net.syscon.elite.security.AuthSource.NOMIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthAwareAuthenticationConverterTest {

    private AuthAwareAuthenticationConverter authenticationConverter = new AuthAwareAuthenticationConverter();
    private Jwt jwt;

    @Before
    public void setUp() {
        jwt = mock(Jwt.class);
    }

    @Test
    public void convert_basicAttributes_attributesCopied() {
        when(jwt.getClaims()).thenReturn(claims("some_user", "auth", "ROLE_some"));

        final var authToken = authenticationConverter.convert(jwt);

        assertThat(authToken.getPrincipal()).isEqualTo("some_user");
        assertThat(authToken.getAuthSource()).isEqualTo(AUTH);
        assertThat(authToken.getAuthorities()).containsExactlyInAnyOrder(new SimpleGrantedAuthority("ROLE_some"));
    }

    @Test
    public void convert_missingUserName_principalIsNull() {
        when(jwt.getClaims()).thenReturn(claims(null, "some_auth_source", "ROLE_some"));

        final var authToken = authenticationConverter.convert(jwt);

        assertThat(authToken.getPrincipal()).isEqualTo(null);
    }

    @Test
    public void convert_missingAuthSource_authSourceIsNomis() {
        when(jwt.getClaims()).thenReturn(claims("some_user", null, "ROLE_some"));

        final var authToken = authenticationConverter.convert(jwt);

        assertThat(authToken.getAuthSource()).isEqualTo(NOMIS);
    }

    @Test
    public void convert_missingAuthorities_noGrantedAuthority() {
        when(jwt.getClaims()).thenReturn(claims("some_user", "some_auth_source", null));

        final var authToken = authenticationConverter.convert(jwt);

        assertThat(authToken.getAuthorities()).isEmpty();
    }

    private Map<String, Object> claims(String username, String authSource, String scope) {
        Map<String, Object> claims = new HashMap<>();
        if (username != null && !username.isBlank()) {
            claims.put("user_name", username);
        }
        if (authSource != null && !authSource.isBlank()) {
            claims.put("auth_source", authSource);
        }
        if (scope != null && !scope.isBlank()) {
            claims.put("authorities", Set.of(scope));
        }
        return claims;
    }
}
