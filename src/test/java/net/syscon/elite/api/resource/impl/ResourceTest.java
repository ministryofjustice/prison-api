package net.syscon.elite.api.resource.impl;

import net.syscon.elite.Elite2ApiServer;
import net.syscon.elite.api.resource.OauthMockServer;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import net.syscon.elite.util.JwtAuthenticationHelper;
import net.syscon.elite.util.JwtParameters;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.core.ResolvableType.forType;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = "test")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Elite2ApiServer.class)
public abstract class ResourceTest {

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @Autowired
    protected AuthTokenHelper authTokenHelper;

    @ClassRule
    public static OauthMockServer oauthMockServer = new OauthMockServer(8080);

    @Before
    public void setUp() {
        oauthMockServer.resetAll();
        oauthMockServer.stubJwkServer();
    }

    protected HttpEntity<?> createHttpEntity(final String bearerToken, final Object body) {
        return createHttpEntity(bearerToken, body, Collections.emptyMap());
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

    private String createJwt(final String user, final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
                .username(user)
                .roles(roles)
                .scope(List.of("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }

    protected <T> void assertThatStatus(final ResponseEntity<T> response, final int status) {
        assertThat(response.getStatusCodeValue()).withFailMessage("Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s", response.getStatusCodeValue(), status, response.getBody()).isEqualTo(status);
    }

    protected void assertThatJsonFileAndStatus(final ResponseEntity<String> response, final int status, final String jsonFile) {
        assertThatStatus(response, status);

        assertThat(getBodyAsJsonContent(response)).isEqualToJson(jsonFile);
    }

    private <T> JsonContent<T> getBodyAsJsonContent(final ResponseEntity<String> response) {
        return new JsonContent<>(getClass(), forType(String.class), Objects.requireNonNull(response.getBody()));
    }
}
