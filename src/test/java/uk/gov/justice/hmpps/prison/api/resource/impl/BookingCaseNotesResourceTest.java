package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

public class BookingCaseNotesResourceTest extends ResourceTest {
    @Test
    public void testCanRetrieveCaseNotes() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);
        CaseNoteSteps
        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/caseNotes",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "case_notes_offender_1.json");
    }

    @Test
    public void testCanFilterCaseNotesByType() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/-2/caseNotes?type=ETE",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "case_notes_offender_filter_by_type.json");
    }

    @Test
    public void testCanFilterCaseNotesBySubType() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/-2/caseNotes?type=COMMS&subType=COM_IN",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "case_notes_offender_filter_by_subtype.json");
    }

    @Test
    public void testCanFilterCaseNotesByDates() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/-2/caseNotes?from=2017-04-06&to=2017-05-05",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "case_notes_offender_filter_by_dates.json");
    }

    @Test
    public void testCanFilterCaseNotesByPrison() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/-3/caseNotes?prisonId=BXI",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "case_notes_offender_filter_by_prison.json");
    }
}
