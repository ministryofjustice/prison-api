package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

public class OffenderMovementsResourceImplIntTest_schedulePrisonToPrisonMove extends ResourceTest {

    private String token;

    @BeforeEach
    public void setup() {
        token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.PRISON_MOVE_MAINTAINER);
    }

    @Test
    public void schedules_prison_to_prison_move() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toPrisonLocation", "BXI",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.scheduledMoveDateTime").isEqualTo("2030-03-11T14:00:00");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.fromPrisonLocation.agencyId").isEqualTo("LEI");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.toPrisonLocation.agencyId").isEqualTo("BXI");
    }

    @Test
    public void schedule_prison_to_prison_move_fails_when_no_matching_booking() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toPrisonLocation", "BXI",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/9999999/prison-to-prison",
                HttpMethod.POST,
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
    public void schedule_prison_to_prison_fails_when_to_prison_does_not_match_booking() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "BXI",
                "toPrisonLocation", "LEI",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Prison to prison move from prison does not match that of the booking.")
                        .developerMessage("Prison to prison move from prison does not match that of the booking.")
                        .build());
    }

    @Test
    public void schedule_prison_to_prison_fails_when_from_prison_not_found() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toPrisonLocation", "PRISON",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Prison with id PRISON not found.")
                        .developerMessage("Prison with id PRISON not found.")
                        .build());
    }

    @Test
    public void schedule_prison_to_prison_fails_when_from_prison_when_unauthorised() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toPrisonLocation", "BXI",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(403)
                        .userMessage("Access is denied")
                        .build());
    }

    @Test
    public void schedule_prison_to_prison_fails_when_from_prison_not_supplied() {
        final var request = createHttpEntity(token, Map.of(
                "toPrisonLocation", "BXI",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The from prison location must be provided");
    }

    @Test
    public void schedule_prison_to_prison_fails_when_to_prison_not_supplied() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The to prison location must be provided");
    }

    @Test
    public void schedule_prison_to_prison_fails_when_escort_type_not_supplied() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toPrisonLocation", "BXI",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The escort type must be provided");
    }

    @Test
    public void schedule_prison_to_prison_fails_when_move_date_time_not_supplied() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toPrisonLocation", "BXI",
                "escortType", "PECS"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The move date time must be provided");
    }

    @Test
    public void schedule_prison_to_prison_fails_when_from_prison_longer_than_6_chars() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEIxxxx",
                "toPrisonLocation", "BXI",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("From prison must be a maximum of 6 characters");
    }

    @Test
    public void schedule_prison_to_prison_fails_when_to_prison_longer_than_6_chars() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toPrisonLocation", "BXIxxxx",
                "escortType", "PECS",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("To prison must be a maximum of 6 characters");
    }

    @Test
    public void schedule_prison_to_prison_fails_when_escort_type_longer_than_12_chars() {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toPrisonLocation", "BXI",
                "escortType", "PECSxxxxxxxxx",
                "scheduledMoveDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-prison",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Escort type must be a maximum of 12 characters");
    }
}
