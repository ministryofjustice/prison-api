package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.AuthLogin;
import net.syscon.elite.web.api.model.Token;
import net.syscon.elite.web.api.model.UserDetails;
import net.thucydides.core.annotations.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for User domain.
 */
public class UserSteps {
    private String token;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${security.authenication.header:Authorization}")
    private String authenicationHeader;

    @Step("User {0} authenticates with password {1}")
    public void authenticates(String username, String password) {
        AuthLogin credentials = new AuthLogin(username, password);
        ResponseEntity<Token> response = restTemplate.exchange("/api/users/login", HttpMethod.POST, createEntity(credentials, null), Token.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        token = response.getBody().getToken();

        assertThat(token).isNotEmpty();
    }

    @Step("Get authentication token")
    public String getToken() {
        return token;
    }

    @Step("Verify current user details")
    public void verifyDetails(String username, String firstName, String lastName) {
        ResponseEntity<UserDetails> response = restTemplate.exchange("/api/users/me", HttpMethod.GET, createEntity(null, null), UserDetails.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserDetails userDetails = response.getBody();

        assertThat(userDetails).hasFieldOrPropertyWithValue("username", username);
        assertThat(userDetails).hasFieldOrPropertyWithValue("firstName", firstName);
        assertThat(userDetails).hasFieldOrPropertyWithValue("lastName", lastName);
    }

    private HttpEntity createEntity(Object entity, Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();

        if (token != null) {
            headers.add(authenicationHeader, token);
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }

        return new HttpEntity<>(entity, headers);
    }
}
