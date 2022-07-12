package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public class BookingResourceCountImplIntTest extends ResourceTest {

    @Test
    public void countPersonalCareNeedsForOffenders() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), List.of("A1234AA", "A1234AD"));

        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/count-personal-care-needs?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonAndStatus(responseEntity, 200, "[{\"offenderNo\":\"A1234AA\",\"size\":4},{\"offenderNo\":\"A1234AD\",\"size\":1}]");
    }

    @Test
    public void countPersonalCareNeedsForOffenders_missingProblemType() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), List.of("A1234AA", "A1234AD"));

        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/count-personal-care-needs?fromStartDate=2010-01-01&toStartDate=2011-01-01", HttpMethod.POST, requestEntity, String.class);
        assertThatStatus(responseEntity, 400);
    }

    @Test
    public void countPersonalCareNeedsForOffenders_emptyBody() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/count-personal-care-needs?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01", HttpMethod.POST, requestEntity, String.class);
        assertThatStatus(responseEntity, 400);
    }
}
