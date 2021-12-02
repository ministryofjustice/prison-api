package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static org.springframework.http.HttpMethod.GET;

public class OffenderSentenceSummaryTest extends ResourceTest {

    @Test
    public void getSentenceSummary() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/booking/latest/sentence-summary",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            "Z0020ZZ");

        assertThatJsonFileAndStatus(response, 200, "sentence_summary.json");
    }

}
