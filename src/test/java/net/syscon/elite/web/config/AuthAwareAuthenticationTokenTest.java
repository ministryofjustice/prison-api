package net.syscon.elite.web.config;

import org.junit.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AuthAwareAuthenticationTokenTest {

    @Test
    public void constructor_nullAuthSource_defaultsToNomis() {
        final var token = new AuthAwareAuthenticationToken(mock(Jwt.class), "any_principal", null, Collections.emptySet());

        assertThat(token.isNomisSource()).isTrue();
    }
}
