package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderSentenceResourceImplIntTest extends ResourceTest {

    @Test
    public void offenderSentence_success() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_GLOBAL_SEARCH"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences?agencyId=LEI", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences.json");
    }

    @Test
    public void offenderSentenceTerms_with_filterParam_success() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_GLOBAL_SEARCH"), Map.of());

        final var url = "/api/offender-sentences/booking/-31/sentenceTerms";

        final var urlBuilder = UriComponentsBuilder.fromPath(url)
                .queryParam("filterBySentenceTermCodes", "IMP")
                .queryParam("filterBySentenceTermCodes", "LIC");

        final var responseEntity = testRestTemplate.exchange(urlBuilder.toUriString(), HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentence_terms_imp_lic.json");
    }


    @Test
    public void retrieveAllSentenceTerms_forABookingId() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_GLOBAL_SEARCH"), Map.of());

        final var url = "/api/offender-sentences/booking/-5/sentenceTerms";
        final var responseEntity = testRestTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentence_terms_imp.json");
    }

    @Test
    public void offenderSentence_400ErrorWhenNoCaseloadsProvided() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_GLOBAL_SEARCH"), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThatStatus(responseEntity, 400);
        assertThat(responseEntity.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Request must be restricted to either a caseload, agency or list of offenders")
                        .developerMessage("400 Request must be restricted to either a caseload, agency or list of offenders")
                        .build());
    }
}
