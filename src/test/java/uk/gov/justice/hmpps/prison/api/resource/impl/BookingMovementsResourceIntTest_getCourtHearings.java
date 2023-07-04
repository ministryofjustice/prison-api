package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingMovementsResourceIntTest_getCourtHearings extends ResourceTest {

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
    public void get_court_hearings_for_booking_returns_3_court_hearings_when_no_dates_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/-3/court-hearings",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "get_court_hearings_3_for_booking.json");
    }

    @Test
    public void get_court_hearings_for_booking_returns_1_court_hearing_when_from_date_limits_results() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/-3/court-hearings?fromDate=2019-05-01",
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
                "/api/bookings/-3/court-hearings?toDate=2016-02-18",
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
                .userMessage("Offender booking with id 666 not found.")
                .developerMessage("Offender booking with id 666 not found.")
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
