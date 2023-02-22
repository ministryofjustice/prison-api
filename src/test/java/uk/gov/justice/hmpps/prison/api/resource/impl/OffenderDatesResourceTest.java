package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.OffenderDatesServiceTest;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

public class OffenderDatesResourceTest extends ResourceTest {

    private static final long BOOKING_ID = -2;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    public void tearDown() {
        // Restore db change as cannot rollback server transaction in client
        // Inserting into the database broke other tests, such as OffendersResourceTest
        // because they depend on seed data from R__4_19__OFFENDER_SENT_CALCULATIONS.sql
        jdbcTemplate.update("DELETE FROM OFFENDER_SENT_CALCULATIONS WHERE OFFENDER_BOOK_ID = -2 AND COMMENT_TEXT LIKE '%Calculate Release Dates%'");
    }

    @Test
    public void testCanUpdateOffenderDates() {
        // Given
        final var token = authTokenHelper.getToken(AuthToken.CRD_USER);
        final var body = RequestToUpdateOffenderDates.builder()
            .keyDates(OffenderDatesServiceTest.createOffenderKeyDates(
                LocalDate.of(2021, 11, 1), LocalDate.of(2021, 11, 2), LocalDate.of(2021, 11, 3),
                LocalDate.of(2021, 11, 4), LocalDate.of(2021, 11, 5), LocalDate.of(2021, 11, 6),
                LocalDate.of(2021, 11, 7), LocalDate.of(2021, 11, 8), LocalDate.of(2021, 11, 9),
                LocalDate.of(2021, 11, 10), LocalDate.of(2021, 11, 11), LocalDate.of(2021, 11, 12),
                LocalDate.of(2021, 11, 13),  LocalDate.of(2021, 11, 14), LocalDate.of(2021, 11, 15), "11/00/00"))
            .submissionUser("ITAG_USER")
            .calculationUuid(UUID.randomUUID())
            .build();
        final var request = createHttpEntity(token, body);

        // When
        final var responseEntity = testRestTemplate.exchange(
            "/api/offender-dates/{bookingId}",
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<String>() {
            },
            Map.of("bookingId", BOOKING_ID));

        // Then
        assertThatJsonFileAndStatus(responseEntity, 201, "offender-key-dates-updated.json");

        final var offenderSentenceResponse = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/sentences",
            GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            "A1234AB");

        assertThatJsonFileAndStatus(offenderSentenceResponse, 200, "sentence-after-offender-key-dates-update.json");

        final var keyDatesAfterUpdate = testRestTemplate.exchange(
            "/api/offender-dates/{bookingId}",
            GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            Map.of("bookingId", BOOKING_ID));

        assertThatJsonFileAndStatus(keyDatesAfterUpdate, 200, "offender-key-dates-after-update.json");
    }


    @Test
    public void testCantUpdateOffenderDatesWithInvalidBookingId() {
        // Given
        final var token = authTokenHelper.getToken(AuthToken.CRD_USER);
        final var body = RequestToUpdateOffenderDates.builder().build();
        final var request = createHttpEntity(token, body);

        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/{bookingId}",
            HttpMethod.POST,
            request,
            ErrorResponse.class,
            Map.of("bookingId", 0));

        // Then
        assertThat(response.getBody()).isEqualTo(
            ErrorResponse.builder()
                .status(404)
                .userMessage("Resource with id [0] not found.")
                .developerMessage("Resource with id [0] not found.")
                .build());
    }

    @Test
    public void testCantUpdateOffenderDatesWithInvalidStaff() {
        // Given
        final var token = authTokenHelper.getToken(AuthToken.CRD_USER);
        final var body = RequestToUpdateOffenderDates.builder()
            .keyDates(OffenderDatesServiceTest.createOffenderKeyDates())
            .submissionUser("fake user")
            .calculationUuid(UUID.randomUUID())
            .build();
        final var request = createHttpEntity(token, body);

        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/{bookingId}",
            HttpMethod.POST,
            request,
            ErrorResponse.class,
            Map.of("bookingId", BOOKING_ID));

        // Then
        assertThat(response.getBody()).isEqualTo(
            ErrorResponse.builder()
                .status(404)
                .userMessage("Resource with id [fake user] not found.")
                .developerMessage("Resource with id [fake user] not found.")
                .build());
    }

    @Test
    public void testCantUpdateOffenderDatesWithIncorrectRole() {
        // Given
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var body = Map.of("some key", "some value");
        final var request = createHttpEntity(token, body);

        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/{bookingId}",
            HttpMethod.POST,
            request,
            ErrorResponse.class,
            Map.of("bookingId", BOOKING_ID));

        // Then
        assertThat(response.getBody()).isEqualTo(
            ErrorResponse.builder()
                .status(403)
                .userMessage("Access Denied")
                .build());
    }


}
