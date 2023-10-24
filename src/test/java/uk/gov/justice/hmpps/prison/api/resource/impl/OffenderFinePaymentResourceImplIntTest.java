package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class OffenderFinePaymentResourceImplIntTest extends ResourceTest {
    @Test
    public void getOffenderSentencesWithOffenceInformation() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), Map.of());

        final var response = testRestTemplate.exchange("/api/offender-fine-payment/booking/-20",
            HttpMethod.GET,
            requestEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "offender-fine-payments.json");
    }

    @Test
    public void getOffenderSentencesWithIncorrectRole() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_WRONG"), Map.of());

        final var response = testRestTemplate.exchange("/api/offender-fine-payment/booking/-20",
            HttpMethod.GET,
            requestEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
    }
}
