package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.AuthLogin;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.Token;
import net.syscon.elite.test.EliteClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static net.syscon.elite.executablespecification.steps.CommonSteps.API_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for API authentication.
 */
public class AuthenticationSteps {
    @Autowired
    protected TestRestTemplate restTemplate;

    @Value("${security.authentication.header:Authorization}")
    private String authenticationHeader;
    private Token token;

    public ErrorResponse authenticate(String username, String password) {
        AuthLogin credentials =
                AuthLogin.builder().username(username).password(password).build();

        try {
            ResponseEntity<Token> response =
                    restTemplate.exchange(API_PREFIX + "users/login", HttpMethod.POST, new HttpEntity<>(credentials), Token.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            token = response.getBody();

            assertThat(token).isNotNull();
        } catch (EliteClientException ex) {
            return ex.getErrorResponse();
        }
        return ErrorResponse.builder().status(HttpStatus.CREATED.value()).build();

    }

    public ErrorResponse refresh() {

        HttpHeaders headers = new HttpHeaders();
        headers.add(getAuthenticationHeader(), getToken().getRefreshToken());
        try {
            ResponseEntity<Token> response =
                    restTemplate.exchange(API_PREFIX + "users/token", HttpMethod.POST, new HttpEntity<>(null, headers), Token.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            token = response.getBody();

            assertThat(token).isNotNull();
        } catch (EliteClientException ex) {
            return ex.getErrorResponse();
        }
        return ErrorResponse.builder().status(HttpStatus.CREATED.value()).build();
    }

    public Token getToken() {
        return token;
    }

    public String getAuthenticationHeader() {
        return authenticationHeader;
    }
}
