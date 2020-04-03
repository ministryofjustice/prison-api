package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderMovementsResourceImplIntTest extends ResourceTest {

    @Test
    public void schedules_court_hearing() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z",
                "comments", "some comments"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 201, "schedule_court_hearing.json");
    }

    @Test
    public void schedule_court_hearing_fails_when_no_matching_booking() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/9999999/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Resource with id [9999999] not found.")
                        .developerMessage("Resource with id [9999999] not found.")
                        .build());
    }

    @Test
    public void schedule_court_hearing_fails_when_no_matching_prison() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISON",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Resource with id [PRISON] not found.")
                        .developerMessage("Resource with id [PRISON] not found.")
                        .build());
    }

    @Test
    public void schedule_court_hearing_fails_when_no_matching_court() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Resource with id [COURT] not found.")
                        .developerMessage("Resource with id [COURT] not found.")
                        .build());
    }

    @Test
    public void schedule_court_hearing_fails_when_no_matching_case_id() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/8888888/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Resource with id [8888888] not found.")
                        .developerMessage("Resource with id [8888888] not found.")
                        .build());
    }

    @Test
    public void schedule_court_hearing_fails_when_unauthorised() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.RENEGADE_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
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
    public void schedules_court_hearing_fails_when_prison_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The from prison location must be provided");
    }

    @Test
    public void schedules_court_hearing_fails_when_court_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The court location to be moved to must be provided");
    }

    @Test
    public void schedules_court_hearing_fails_when_date_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The future court hearing date time must be provided");
    }

    @Test
    public void schedules_court_hearing_fails_when_prison_longer_than_6_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISONx",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("From location must be a maximum of 6 characters");
    }

    @Test
    public void schedules_court_hearing_fails_when_court_longer_than_6_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISON",
                "toCourtLocation", "COURT1x",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("To location must be a maximum of 6 characters");
    }

    @Test
    public void schedules_court_hearing_fails_when_comments_longer_than_240_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z",
                "comments", StringUtils.repeat("a", 241)
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-cases/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Comment text must be a maximum of 240 characters");
    }

    @Test
    public void get_court_hearings_for_booking_returns_no_court_hearings() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/-41/court-hearings",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "get_court_hearings_for_booking_none_found.json");
    }

    @Test
    public void get_court_hearings_for_booking_returns_2_court_hearings_when_no_dates_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-hearings",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "get_court_hearings_2_for_booking.json");
    }

    @Test
    public void get_court_hearings_for_booking_returns_1_court_hearing_when_from_date_limits_results() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-hearings?fromDate=2017-02-18",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "get_court_hearings_1_for_booking.json");
    }

    @Test
    public void get_court_hearings_for_booking_returns_no_bookings_when_none_in_date_range() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-hearings?toDate=2016-02-18",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "get_court_hearings_for_booking_none_found.json");
    }

    @Test
    public void get_court_hearings_fails_when_no_matching_booking() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/666/court-hearings",
                HttpMethod.GET,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(ErrorResponse.builder()
                .status(404)
                .userMessage("Resource with id [666] not found.")
                .developerMessage("Resource with id [666] not found.")
                .build());
    }

    @Test
    public void get_court_hearings_for_booking_fails_on_invalid_date_range() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-hearings?fromDate=2020-03-23&toDate=2020-03-22",
                HttpMethod.GET,
                request, ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(ErrorResponse.builder()
                .status(400)
                .userMessage("Invalid date range: toDate is before fromDate.")
                .developerMessage("Invalid date range: toDate is before fromDate.")
                .build());
    }
}
