package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class OffenderMovementsResourceImplIntTest_rescheduleCourtHearing extends ResourceTest {

    @Test
    public void reschedule_court_hearing() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/bookings/-8/prison-to-court-hearings/-208/revised-hearing-date/2030-03-11T14:00",
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.dateTime").isEqualTo("2030-03-11T14:00:00");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathBooleanValue("$.location.active").isEqualTo(true);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.location.agencyId").isEqualTo("COURT1");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.location.agencyType").isEqualTo("CRT");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.location.description").isEqualTo("Court 1");
    }

    @Test
    public void reschedule_court_hearing_fails_when_no_matching_booking() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/bookings/9999999/prison-to-court-hearings/-1/revised-hearing-date/2030-03-11T14:00",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Offender booking with id 9999999 not found.")
                        .developerMessage("Offender booking with id 9999999 not found.")
                        .build());
    }

    @Test
    public void reschedule_court_hearing_fails_when_date_in_past() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/bookings/-8/prison-to-court-hearings/-208/revised-hearing-date/1970-01-01T00:00",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Revised court hearing date '1970-01-01T00:00' must be in the future.")
                        .developerMessage("Revised court hearing date '1970-01-01T00:00' must be in the future.")
                        .build());
    }

    @Test
    public void schedules_court_hearing_fails_when_unauthorised() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.RENEGADE_USER);

        final var request = createHttpEntity(token, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/bookings/9999999/prison-to-court-hearings/-1/revised-hearing-date/2030-03-11T14:00",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(403)
                        .userMessage("Access is denied")
                        .build());
    }

    @Test
    public void reschedule_court_hearing_fails_when_hearing_in_past() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/bookings/-8/prison-to-court-hearings/-209/revised-hearing-date/2030-03-11T14:00",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("The existing court hearing '-209' must be in a scheduled state to reschedule.")
                        .developerMessage("The existing court hearing '-209' must be in a scheduled state to reschedule.")
                        .build());
    }
}
