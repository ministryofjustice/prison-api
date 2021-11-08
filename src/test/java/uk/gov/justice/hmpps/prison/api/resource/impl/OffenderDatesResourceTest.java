package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.OffenderDatesServiceTest;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderDatesResourceTest extends ResourceTest {

    private static final LocalDate NOV_11_2021 = LocalDate.of(2021, 11, 8);

    @Test
    public void testCanUpdateOffenderDates() {
        final var token = authTokenHelper.getToken(AuthToken.CRD_USER);
        final var body = RequestToUpdateOffenderDates.builder()
            .keyDates(OffenderDatesServiceTest.createOffenderKeyDates(NOV_11_2021, NOV_11_2021, NOV_11_2021))
            .build();
        final var request = createHttpEntity(token, body);

        final var responseEntity = testRestTemplate.exchange(
            "/api/offender-dates/{bookingId}",
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<String>() {
            },
            -2);

        assertThatJsonFileAndStatus(responseEntity, 201, "offenderkeydatesupdated.json");
    }

    @Test
    public void testCantUpdateOffenderDatesWithInvalidBookingId() {
        final var token = authTokenHelper.getToken(AuthToken.CRD_USER);
        final var body = Map.of("some key", "some value");
        final var request = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/offender-dates/{bookingId}",
            HttpMethod.POST,
            request,
            ErrorResponse.class,0,-11);

        assertThat(response.getBody()).isEqualTo(
            ErrorResponse.builder()
                .status(404)
                .userMessage("Resource with id [0] not found.")
                .developerMessage("Resource with id [0] not found.")
                .build());
    }

    @Test
    public void testCantUpdateOffenderDatesWithIncorrectRole() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var body = Map.of("some key", "some value");
        final var request = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/offender-dates/{bookingId}",
            HttpMethod.POST,
            request,
            ErrorResponse.class,0,-11);

        assertThat(response.getBody()).isEqualTo(
            ErrorResponse.builder()
                .status(403)
                .userMessage("Access is denied")
                .build());
    }
}