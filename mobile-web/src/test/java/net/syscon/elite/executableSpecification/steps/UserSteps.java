package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.UserDetails;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for User domain.
 */
public class UserSteps extends CommonSteps {
    @Step("Verify current user details")
    public void verifyDetails(String username, String firstName, String lastName) {
        ResponseEntity<UserDetails> response = restTemplate.exchange("/api/users/me", HttpMethod.GET, createEntity(), UserDetails.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserDetails userDetails = response.getBody();

        assertThat(userDetails).hasFieldOrPropertyWithValue("username", username);
        assertThat(userDetails).hasFieldOrPropertyWithValue("firstName", firstName);
        assertThat(userDetails).hasFieldOrPropertyWithValue("lastName", lastName);
    }
}
