package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

public class OffenderMovementsResourceIntTest_scheduleCourtHearing extends ResourceTest {

    @Test
    public void schedules_court_hearing() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z",
                "comments", "some comments"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<String>() {
                });


        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.id").isNotNull();
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.dateTime").isEqualTo("2030-03-11T14:00:00");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathBooleanValue("$.location.active").isEqualTo(true);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.location.agencyId").isEqualTo("COURT1");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.location.agencyType").isEqualTo("CRT");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.location.description").isEqualTo("Court 1");
    }

    @Test
    public void schedules_court_hearing_fails_when_no_matching_booking() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/9999999/prison-to-court-hearings",
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
    public void schedules_court_hearing_fails_when_no_matching_prison() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISON",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
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
    public void schedules_court_hearing_fails_when_no_matching_court() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Court with id COURT not found.")
                        .developerMessage("Court with id COURT not found.")
                        .build());
    }

    @Test
    public void schedules_case_hearing_fails_when_unauthorised() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(403)
                        .userMessage("Access Denied")
                        .build());
    }

    @Test
    public void schedules_court_hearing_fails_when_prison_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Field: fromPrisonLocation - The from prison location must be provided.")
                        .developerMessage("Field: fromPrisonLocation - The from prison location must be provided.")
                        .build());
    }

    @Test
    public void schedules_court_hearing_fails_when_court_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Field: toCourtLocation - The court location to be moved to must be provided.")
                        .developerMessage("Field: toCourtLocation - The court location to be moved to must be provided.")
                        .build());
    }

    @Test
    public void schedules_court_hearing_fails_when_prison_and_court_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "courtHearingDateTime", "2030-03-11T14:00:00"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Field: fromPrisonLocation - The from prison location must be provided.");
        assertThat(error.getUserMessage()).contains("Field: toCourtLocation - The court location to be moved to must be provided.");
    }

    @Test
    public void schedules_court_hearing_fails_when_date_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Field: courtHearingDateTime - The future court hearing date time must be provided.")
                        .developerMessage("Field: courtHearingDateTime - The future court hearing date time must be provided.")
                        .build());
    }

    @Test
    public void schedules_court_hearing_fails_when_prison_longer_than_6_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISONx",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Field: fromPrisonLocation - From location must be a maximum of 6 characters.")
                        .developerMessage("Field: fromPrisonLocation - From location must be a maximum of 6 characters.")
                        .build());
    }

    @Test
    public void schedules_court_hearing_fails_when_court_longer_than_6_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISON",
                "toCourtLocation", "COURT1x",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Field: toCourtLocation - To location must be a maximum of 6 characters.")
                        .developerMessage("Field: toCourtLocation - To location must be a maximum of 6 characters.")
                        .build());
    }

    @Test
    public void schedules_court_hearing_fails_when_comments_longer_than_240_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z",
                "comments", StringUtils.repeat("a", 241)
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Field: comments - Comment text must be a maximum of 240 characters.")
                        .developerMessage("Field: comments - Comment text must be a maximum of 240 characters.")
                        .build());
    }
}
