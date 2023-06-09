package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;

public class CourtDateResourceImplIntTest extends ResourceTest {
    @Test
    public void courtDateResults() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_MANAGE_DIGITAL_WARRANT"), null);

        final var responseEntity = testRestTemplate
            .exchange(
                "/api/court-date-results/Z0020XY",
                HttpMethod.GET,
                requestEntity,
                String.class
            );

        assertThatJsonFileAndStatus(responseEntity, HttpStatus.OK.value(), "court-date-results.json");
    }
}
