package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.justice.hmpps.prison.PrisonApiServer;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository;
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper;
import uk.gov.justice.hmpps.prison.util.JwtParameters;
import uk.gov.justice.hmpps.prison.util.builders.TestDataContext;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.core.ResolvableType.forType;

@ActiveProfiles(value = "test")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = PrisonApiServer.class)
@AutoConfigureTestEntityManager
public abstract class ResourceTest {
    @Autowired
    private DataLoaderRepository dataLoader;

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @Autowired
    protected AuthTokenHelper authTokenHelper;

    protected TestDataContext getTestDataContext() {
        return new TestDataContext(webTestClient, jwtAuthenticationHelper, dataLoader);
    }

    protected HttpEntity<?> createHttpEntity(final String bearerToken, final Object body) {
        return createHttpEntity(bearerToken, body, Collections.emptyMap());
    }

    protected HttpEntity<?> createHttpEntity(final AuthToken authToken, final Object body) {
        return createHttpEntity(authTokenHelper.getToken(authToken), body, Collections.emptyMap());
    }
    protected HttpEntity<?> createEmptyHttpEntity(final AuthToken authToken) {
        return createHttpEntity(authTokenHelper.getToken(authToken), null, Collections.emptyMap());
    }

    protected HttpEntity<?> createEmptyHttpEntity(final AuthToken authToken, final Map<String, String> additionalHeaders) {
        return createHttpEntity(authTokenHelper.getToken(authToken), null, additionalHeaders);
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

    protected String validToken(final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read", "write"))
                        .roles(roles)
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

    protected String clientToken(final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(
            JwtParameters.builder()
                .scope(List.of("read", "write"))
                .roles(roles)
                .expiryTime(Duration.ofDays(365 * 10))
                .build()
        );
    }

    protected <T> void assertThatStatus(final ResponseEntity<T> response, final int status) {
        assertThatStatus(response, HttpStatusCode.valueOf(status));
    }

    protected <T> void assertThatStatus(final ResponseEntity<T> response, final HttpStatusCode status) {
        assertThat(response.getStatusCode()).withFailMessage("Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s", response.getStatusCode(), status, response.getBody()).isEqualTo(status);
    }

    protected void assertThatJsonFileAndStatus(final ResponseEntity<String> response, final int status, final String jsonFile) {
        assertThatJsonFileAndStatus(response, HttpStatusCode.valueOf(status), jsonFile);
    }

    protected void assertThatJsonFileAndStatus(final ResponseEntity<String> response, final HttpStatusCode status, final String jsonFile) {
        assertThatStatus(response, status);

        final var bodyAsJsonContent = getBodyAsJsonContent(response);
        assertThat(bodyAsJsonContent).isEqualToJson(jsonFile);
    }

    protected void assertThatJsonAndStatus(final ResponseEntity<String> response, final int status, final String json) {
        assertThatStatus(response, status);

        assertThatJson(response.getBody()).isEqualTo(json);
    }

    protected void assertThatOKResponseContainsJson(final ResponseEntity<String> response, final String json) {
        assertThatStatus(response, 200);

        assertThat(getBodyAsJsonContent(response)).isEqualToJson(json);
    }

    protected <T> JsonContent<T> getBodyAsJsonContent(final ResponseEntity<String> response) {
        return new JsonContent<>(getClass(), forType(String.class), Objects.requireNonNull(response.getBody()));
    }

    protected Consumer<HttpHeaders> setAuthorisation(List<String> roles) {
        return (httpHeaders -> httpHeaders.add("Authorization", "Bearer " + validToken(roles)));
    }

    protected Consumer<HttpHeaders> setAuthorisation(String username, List<String> roles) {
        return (httpHeaders -> httpHeaders.add("Authorization", "Bearer " + createJwt(username, roles)));
    }

    protected Consumer<HttpHeaders> setClientAuthorisation(List<String> roles) {
        return (httpHeaders -> httpHeaders.add("Authorization", "Bearer " + clientToken(roles)));
    }
}
