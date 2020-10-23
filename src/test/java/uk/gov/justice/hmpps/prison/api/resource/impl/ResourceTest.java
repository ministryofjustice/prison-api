package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.PrisonApiServer;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper;
import uk.gov.justice.hmpps.prison.util.JwtParameters;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.core.ResolvableType.forType;

@ActiveProfiles(value = "test")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = PrisonApiServer.class)
public abstract class ResourceTest {

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @Autowired
    protected AuthTokenHelper authTokenHelper;

    protected HttpEntity<?> createHttpEntity(final String bearerToken, final Object body) {
        return createHttpEntity(bearerToken, body, Collections.emptyMap());
    }

    protected HttpEntity<?> createHttpEntity(final AuthToken authToken, final Object body) {
        return createHttpEntity(authTokenHelper.getToken(authToken), body, Collections.emptyMap());
    }

    protected HttpEntity<?> createHttpEntity(final String bearerToken, final Object body, final Map<String, String> additionalHeaders) {
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

    protected String createJwt(final String user, final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
                .username(user)
                .roles(roles)
                .scope(List.of("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }

    protected String validToken() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    protected String readOnlyToken() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read"))
                        .roles(List.of())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    protected <T> void assertThatStatus(final ResponseEntity<T> response, final int status) {
        assertThat(response.getStatusCodeValue()).withFailMessage("Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s", response.getStatusCodeValue(), status, response.getBody()).isEqualTo(status);
    }

    protected void assertThatJsonFileAndStatus(final ResponseEntity<String> response, final int status, final String jsonFile) {
        assertThatStatus(response, status);

        assertThat(getBodyAsJsonContent(response)).isEqualToJson(jsonFile);
    }

    protected void assertThatJsonAndStatus(final ResponseEntity<String> response, final int status, final String json) {
        assertThatStatus(response, status);

        assertThatJson(response.getBody()).isEqualTo(json);
    }

    protected <T> JsonContent<T> getBodyAsJsonContent(final ResponseEntity<String> response) {
        return new JsonContent<>(getClass(), forType(String.class), Objects.requireNonNull(response.getBody()));
    }
}
