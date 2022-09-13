package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;
import uk.gov.justice.hmpps.prison.util.PropertiesUtil;

import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

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
}
