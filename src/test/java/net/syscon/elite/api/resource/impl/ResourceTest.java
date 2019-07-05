package net.syscon.elite.api.resource.impl;

import net.syscon.elite.util.JwtAuthenticationHelper;
import net.syscon.elite.util.JwtParameters;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = "test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class ResourceTest {

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    protected HttpEntity<?> createHttpEntity(final String bearerToken, final Object body) {
        return createHttpEntity(bearerToken, body, Collections.emptyMap());
    }

    private HttpEntity<?> createHttpEntity(final String bearerToken, final Object body, final Map<String, String> additionalHeaders) {
        final var headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + bearerToken);
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        additionalHeaders.forEach(headers::add);

        return new HttpEntity<>(body, headers);
    }

    protected HttpEntity<?> createHttpEntityWithBearerAuthorisationAndBody(final String user, final List<String> roles, final Object body) {
        final var jwt = createJwt(user, roles);
        return createHttpEntity(jwt, body);
    }

    protected HttpEntity<?> createHttpEntityWithBearerAuthorisation(final String user, final List<String> roles, final Map<String, String> additionalHeaders) {
        final var jwt = createJwt(user, roles);
        return createHttpEntity(jwt, null, additionalHeaders == null ? Map.of() : additionalHeaders);
    }

    private String createJwt(final String user, final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
                .username(user)
                .roles(roles)
                .scope(List.of("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }
}
