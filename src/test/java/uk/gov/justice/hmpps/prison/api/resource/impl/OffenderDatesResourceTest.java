package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderCalculatedKeyDates;
import uk.gov.justice.hmpps.prison.api.model.LatestTusedData;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalculationSummary;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.OffenderDatesServiceTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void testGetAllCalculationsForPrisoner() {
        // Given
        final var request = createEmptyHttpEntity(AuthToken.CRD_USER);
        final var type = new ParameterizedTypeReference<List<SentenceCalculationSummary>>() {};
        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/calculations/{nomsId}",
            HttpMethod.GET,
            request,
            type,
            Map.of("nomsId", "A1234AA"));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        var calculationSummary = response.getBody().getFirst();
        assertThat(calculationSummary.getOffenderNo()).isEqualTo("A1234AA");
        assertThat(calculationSummary.getLastName()).isEqualTo("ANDERSON");
        assertThat(calculationSummary.getAgencyDescription()).isEqualTo("LEEDS");
        assertThat(calculationSummary.getCommentText()).isEqualTo("Some Comment Text");
        assertThat(calculationSummary.getCalculationReason()).isEqualTo("New Sentence");
    }

    @Test
    public void testGetOffenderKeyDatesForOffenderSentCalcId() {
        // Given
        final var request = createEmptyHttpEntity(AuthToken.CRD_USER);
        final var type = new ParameterizedTypeReference<OffenderCalculatedKeyDates>() {};
        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/sentence-calculation/{offenderSentCalcId}",
            GET,
            request,
            type,
            Map.of("offenderSentCalcId", "-16"));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var nomisCalculations = response.getBody();
        assertThat(nomisCalculations.getReasonCode()).isEqualTo("NEW");
        assertThat(nomisCalculations.getComment()).isNull();
        assertThat(nomisCalculations.getParoleEligibilityDate()).isNull();
        assertThat(nomisCalculations.getApprovedParoleDate()).isNull();
        assertThat(nomisCalculations.getConditionalReleaseDate()).isNull();
        assertThat(nomisCalculations.getReleaseOnTemporaryLicenceDate()).isNull();
        assertThat(nomisCalculations.getAutomaticReleaseDate()).isNull();
        assertThat(nomisCalculations.getLateTermDate()).isNull();
        assertThat(nomisCalculations.getPostRecallReleaseDate()).isNull();
        assertThat(nomisCalculations.getTariffDate()).isNull();
        assertThat(nomisCalculations.getEffectiveSentenceEndDate()).isNull();
        assertThat(nomisCalculations.getDtoPostRecallReleaseDate()).isNull();
        assertThat(nomisCalculations.getEarlyRemovalSchemeEligibilityDate()).isNull();
        assertThat(nomisCalculations.getTariffExpiredRemovalSchemeEligibilityDate()).isNull();
        assertThat(nomisCalculations.getTopupSupervisionExpiryDate()).isNull();
        assertThat(nomisCalculations.getNonParoleDate()).isNull();
        assertThat(nomisCalculations.getSentenceExpiryDate()).isEqualTo(LocalDate.of(2022, 10, 20));
        assertThat(nomisCalculations.getLicenceExpiryDate()).isEqualTo(LocalDate.of(2021, 9, 24));
        assertThat(nomisCalculations.getMidTermDate()).isEqualTo(LocalDate.of(2021, 3, 25));
        assertThat(nomisCalculations.getEarlyTermDate()).isEqualTo(LocalDate.of(2021, 2, 28));
        assertThat(nomisCalculations.getHomeDetentionCurfewApprovedDate()).isEqualTo(LocalDate.of(2021, 1, 2));
        assertThat(nomisCalculations.getCalculatedAt()).isEqualTo(LocalDateTime.of(2017, 9, 2, 0, 0));
        assertThat(nomisCalculations.getHomeDetentionCurfewEligibilityDate()).isEqualTo(LocalDate.of(2020, 12, 30));
    }

    @Test
    public void testGetLatestTusedForPrisoner() {
        // Given
        final var request = createEmptyHttpEntity(AuthToken.CRD_USER);
        final var type = new ParameterizedTypeReference<LatestTusedData>() {};
        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/latest-tused/{nomsId}",
            HttpMethod.GET,
            request,
            type,
            Map.of("nomsId", "A1234AF"));

        // Then
        LatestTusedData latestTusedData = response.getBody();
        assertThat(latestTusedData.getOffenderNo()).isEqualTo("A1234AF");
        assertThat(latestTusedData.getLatestTused()).isEqualTo(LocalDate.of(2021,3,30));
    }

    @Test
    public void testGetLatestTusedForPrisonerWithOverridenTused() {
        // Given
        final var request = createEmptyHttpEntity(AuthToken.CRD_USER);
        final var type = new ParameterizedTypeReference<LatestTusedData>() {};
        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/latest-tused/{nomsId}",
            HttpMethod.GET,
            request,
            type,
            Map.of("nomsId", "A1234AG"));

        // Then
        LatestTusedData latestTusedData = response.getBody();
        assertThat(latestTusedData.getOffenderNo()).isEqualTo("A1234AG");
        assertThat(latestTusedData.getLatestTused()).isEqualTo(LocalDate.of(2021,3,30));
        assertThat(latestTusedData.getLatestOverrideTused()).isEqualTo(LocalDate.of(2021,3,31));
    }
    @Test
    public void testGetLatestTusedForPrisonerThatDoesntExist() {
        // Given
        final var request = createEmptyHttpEntity(AuthToken.CRD_USER);
        final var type = new ParameterizedTypeReference<LatestTusedData>() {};
        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/latest-tused/{nomsId}",
            HttpMethod.GET,
            request,
            type,
            Map.of("nomsId", "doesntExist"));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetLatestTusedForPrisonerWithNoTused() {
        // Given
        final var request = createEmptyHttpEntity(AuthToken.CRD_USER);
        final var type = new ParameterizedTypeReference<LatestTusedData>() {};
        // When
        final var response = testRestTemplate.exchange(
            "/api/offender-dates/latest-tused/{nomsId}",
            HttpMethod.GET,
            request,
            type,
            Map.of("nomsId", "A1234AA"));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
