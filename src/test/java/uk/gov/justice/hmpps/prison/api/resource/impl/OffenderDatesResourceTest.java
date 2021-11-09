package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.OffenderDatesServiceTest;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Make these tests transactional because inserting into the database broke other tests, such as OffendersResourceTest
// because they depend on seed data from R__4_19__OFFENDER_SENT_CALCULATIONS.sql
@Transactional
public class OffenderDatesResourceTest extends ResourceTest {

    private static final long BOOKING_ID = -2;
    private static final LocalDate NOV_11_2021 = LocalDate.of(2021, 11, 8);

    @Test
    public void testCanUpdateOffenderDates() {
        // Given
        final var token = authTokenHelper.getToken(AuthToken.CRD_USER);
        final var body = RequestToUpdateOffenderDates.builder()
            .keyDates(OffenderDatesServiceTest.createOffenderKeyDates(NOV_11_2021, NOV_11_2021, NOV_11_2021))
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
        assertThatJsonFileAndStatus(responseEntity, 201, "offenderkeydatesupdated.json");
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
                .userMessage("Access is denied")
                .build());
    }
}