package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.AuthLogin;
import net.syscon.elite.web.api.model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for API authentication.
 */
public class AuthenticationSteps {
    @Autowired
    protected TestRestTemplate restTemplate;

    @Value("${security.authentication.header:Authorization}")
    private String authenticationHeader;

    private String token;

    public void authenticate(String username, String password) {
        AuthLogin credentials = new AuthLogin(username, password);

        ResponseEntity<Token> response =
                restTemplate.exchange("/api/users/login", HttpMethod.POST, new HttpEntity<>(credentials), Token.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        token = response.getBody().getToken();

        assertThat(token).isNotEmpty();
    }

    public String getToken() {
        return token;
    }

    public String getAuthenticationHeader() {
        return authenticationHeader;
    }
}
