package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.springframework.http.HttpStatus.OK;

public class OffenderMovementsResourceImplIntTest_rescheduleCourtHearing extends ResourceTest {

    @Test
    public void reschedule_court_hearing() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var initialHearingDateTime = LocalDateTime.of(2050, 3, 11, 12, 0, 0);

        final var scheduledHearing = scheduleHearingToAmend(token, PrisonToCourtHearing.builder()
                .fromPrisonLocation("LEI")
                .toCourtLocation("COURT1")
                .courtHearingDateTime(LocalDateTime.of(2050, 3, 11, 12, 0, 0))
                .build());

        assertThat(scheduledHearing.getDateTime()).isEqualTo(initialHearingDateTime);

        final var request = createHttpEntity(token, Map.of("revisedHearingDateTime", "2030-03-11T14:00"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings/" + scheduledHearing.getId() + "/revised-hearing-date",
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<CourtHearing>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getId()).isEqualTo(scheduledHearing.getId());
        assertThat(response.getBody().getDateTime()).isEqualTo("2030-03-11T14:00:00");
    }

    private CourtHearing scheduleHearingToAmend(final String token, final PrisonToCourtHearing hearing) {
        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", hearing.getFromPrisonLocation(),
                "toCourtLocation", hearing.getToCourtLocation(),
                "courtHearingDateTime", hearing.getCourtHearingDateTime()
        ));

        return testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<CourtHearing>() {
                }).getBody();
    }

    @Test
    public void reschedule_court_hearing_fails_when_no_matching_booking() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of("revisedHearingDateTime", "2030-03-11T14:00"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/9999999/prison-to-court-hearings/-1/revised-hearing-date",
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

        final var request = createHttpEntity(token, Map.of("revisedHearingDateTime", "1970-01-01T00:00"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-8/prison-to-court-hearings/-208/revised-hearing-date",
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

        final var request = createHttpEntity(token, Map.of("revisedHearingDateTime", "2030-03-11T14:00"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/9999999/prison-to-court-hearings/-1/revised-hearing-date",
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

        final var request = createHttpEntity(token, Map.of("revisedHearingDateTime", "2030-03-11T14:00"));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-8/prison-to-court-hearings/-209/revised-hearing-date",
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
