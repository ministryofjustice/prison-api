package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

public class AdjustmentResourceTest extends ResourceTest {
    private static final long BOOKING_ID = -6;

    @Test
    public void returnsExpectedValue() {
        final var token = authTokenHelper.getToken(AuthToken.CRD_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/adjustments/" + BOOKING_ID + "/sentence-and-booking",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            }
        );

        assertThatJsonFileAndStatus(response, 200, "booking-and-sentence-adjustments.json");
    }
}
