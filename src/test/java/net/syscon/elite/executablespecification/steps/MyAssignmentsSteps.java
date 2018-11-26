package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.OffenderBooking;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for my assignments.
 */
public class MyAssignmentsSteps extends CommonSteps {
    private static final String API_MY_ASSIGNMENTS_URL = API_PREFIX + "users/me/bookingAssignments";

    @Step("Retrieve my assignments")
    public void getMyAssignments() {
        init();
        ResponseEntity<List<OffenderBooking>> response = restTemplate.exchange(API_MY_ASSIGNMENTS_URL,
                HttpMethod.GET, createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<List<OffenderBooking>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        buildResourceData(response);
    }
}
