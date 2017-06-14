package net.syscon.elite.executableSpecification.steps;

import net.thucydides.core.annotations.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common BDD step implementations
 */
public abstract class CommonSteps {
    @Autowired
    private AuthenticationSteps auth;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Step("User {0} authenticates with password {1}")
    public void authenticates(String username, String password) {
        auth.authenticate(username, password);
    }

    @Step("Verify authentication token")
    public void verifyToken() {
        assertThat(auth.getToken()).isNotEmpty();
    }

    protected HttpEntity createEntity() {
        return createEntity(null, null);
    }

    protected HttpEntity createEntity(Object entity) {
        return createEntity(entity, null);
    }

    protected HttpEntity createEntity(Object entity, Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();

        if (auth.getToken() != null) {
            headers.add(auth.getAuthenticationHeader(), auth.getToken());
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }

        return new HttpEntity<>(entity, headers);
    }
}
