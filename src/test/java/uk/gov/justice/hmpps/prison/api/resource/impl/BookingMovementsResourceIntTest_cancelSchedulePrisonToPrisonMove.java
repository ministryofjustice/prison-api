package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class BookingMovementsResourceIntTest_cancelSchedulePrisonToPrisonMove extends ResourceTest {

    private String token;

    @BeforeEach
    public void setup() {
        token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.PRISON_MOVE_MAINTAINER);
    }

    @Test
    public void cancels_prison_to_prison_move() {
        final var request = createHttpEntity(token, Map.of("reasonCode", "ADMI"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-3/prison-to-prison/-26/cancel",
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
    }


    @Test
    public void cancels_prison_to_prison_move_fails_for_unknown_cancellation_reason() {
        final var request = createHttpEntity(token, Map.of("reasonCode", "ADMIX"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-3/prison-to-prison/-27/cancel",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);


        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Cancellation reason ADMIX not found.")
                        .developerMessage("Cancellation reason ADMIX not found.")
                        .build());
    }

    @Test
    public void cancel_prison_to_prison_move_fails_when_no_matching_booking() {
        final var request = createHttpEntity(token, Map.of("reasonCode", "ADMI"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-99999/prison-to-prison/-26/cancel",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Offender booking with id -99999 not found.")
                        .developerMessage("Offender booking with id -99999 not found.")
                        .build());
    }

    @Test
    public void cancel_prison_to_prison_move_fails_when_event_booking_does_not_match_booking() {
        final var request = createHttpEntity(token, Map.of("reasonCode", "ADMI"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison/-26/cancel",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Booking with id -1 not associated with the supplied move id -26.")
                        .developerMessage("Booking with id -1 not associated with the supplied move id -26.")
                        .build());
    }

    @Test
    public void schedule_prison_to_prison_fails_when_from_prison_when_unauthorised() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of("reasonCode", "ADMI"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-3/prison-to-prison/-26/cancel",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(403)
                        .userMessage("Access Denied")
                        .build());
    }

    @Test
    public void cancel_prison_to_prison_move_fails_when_no_matching_move() {
        final var request = createHttpEntity(token, Map.of("reasonCode", "ADMI"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-3/prison-to-prison/-88888/cancel",
                HttpMethod.PUT,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Scheduled prison move with id -88888 not found.")
                        .developerMessage("Scheduled prison move with id -88888 not found.")
                        .build());
    }
}
