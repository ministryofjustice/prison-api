package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

import java.util.List;
import java.util.Map;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class OffenderSentenceResourceImplIntTest extends ResourceTest {

    @Test
    public void offenderSentence_success() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences?agencyId=LEI", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences.json");
    }

    @Test
    public void offenderSentence_unknownUser_noRoles_onlyAgency() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("UNKNOWN_USER", List.of(), null);
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences?agencyId=LEI", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonAndStatus(responseEntity, HTTP_OK, "[]");
    }

    @Test
    public void offenderSentence_unknownUser_noRoles_singleOffender() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("UNKNOWN_USER", List.of(), null);
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences?offenderNo=A1234AH", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonAndStatus(responseEntity, HTTP_OK, "[]");
    }

    @Test
    public void offenderSentence_unknownUser_viewRole_onlyAgency() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("UNKNOWN_USER", List.of("ROLE_VIEW_PRISONER_DATA"), null);
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences?agencyId=LEI", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences.json");
    }

    @Test
    public void offenderSentence_unknownUser_viewRole_singleOffender() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("UNKNOWN_USER", List.of("ROLE_VIEW_PRISONER_DATA"), null);
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences?offenderNo=A1234AH", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json");
    }

    @Test
    public void offenderSentence_noRoles_onlyAgency() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), null);
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences?agencyId=LEI", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences.json");
    }

    @Test
    public void offenderSentence_noRoles_singleOffender() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), null);
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences?offenderNo=A1234AH", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json");
    }

    @Test
    public void offenderSentenceTerms_with_filterParam_success() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), Map.of());

        final var url = "/api/offender-sentences/booking/-31/sentenceTerms";

        final var urlBuilder = UriComponentsBuilder.fromPath(url)
                .queryParam("filterBySentenceTermCodes", "IMP")
                .queryParam("filterBySentenceTermCodes", "LIC");

        final var responseEntity = testRestTemplate.exchange(urlBuilder.toUriString(), HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentence_terms_imp_lic.json");
    }


    @Test
    public void retrieveAllSentenceTerms_forABookingId() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), Map.of());

        final var url = "/api/offender-sentences/booking/-5/sentenceTerms";
        final var responseEntity = testRestTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentence_terms_imp.json");
    }

    @Test
    public void offenderSentence_400ErrorWhenNoCaseloadsProvided() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThatStatus(responseEntity, 400);
        assertThat(responseEntity.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Request must be restricted to either a caseload, agency or list of offenders")
                        .developerMessage("400 Request must be restricted to either a caseload, agency or list of offenders")
                        .build());
    }

    @Test
    public void postOffenderSentence_success() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of("A1234AH"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json");
    }

    @Test
    public void postOffenderSentence_success_multiple() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"),
            List.of("A1234AH", "A6676RS", "A1234AG", "A1234AI", "Z0017ZZ", "A1234AJ", "A1234AC", "A1234AL", "A1234AK", "A1234AB", "A1234AA", "A1234AF", "Z0018ZZ", "A4476RS", "A1234AD", "Z0024ZZ", "Z0025ZZ", "A1234AE", "A9876RS", "A9876EC", "A1178RS", "A5577RS", "A1176RS", "A5576RS", "Z0019ZZ", "G2823GV", "A1060AA"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences.json");
    }

    @Test
    public void postOffenderSentence_unknownUser_noRoles_singleOffender() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("UNKNOWN_USER", List.of(), List.of("A1234AH"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonAndStatus(responseEntity, HTTP_OK, "[]");
    }

    @Test
    public void postOffenderSentence_unknownUser_viewRole_singleOffender() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("UNKNOWN_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of("A1234AH"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json");
    }

    @Test
    public void postOffenderSentence_noRoles_singleOffender() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), List.of("A1234AH"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json");
    }

    @Test
    public void postOffenderSentence_noOffenders() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of());
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonAndStatus(responseEntity, 400, """
                    {"status":400,"userMessage":"List of Offender Ids must be provided","developerMessage":"400 List of Offender Ids must be provided"}
            """);
    }

    @Test
    public void postOffenderSentenceBookings_success() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of("-8"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json");
    }

    @Test
    public void postOffenderSentenceBookings_acrossCaseloadsAsSystemUser() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of("-1", "-16", "-36"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String.class);

        assertThatOKResponseContainsJson(responseEntity, """
                [{"offenderNo":"A1180MA"},{"offenderNo":"A1234AA"},{"offenderNo":"A1234AP"}]
            """);
    }

    @Test
    public void postOffenderSentenceBookings_bookingIdsAcrossCaseloads() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), List.of("-11", "-5", "-16"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String.class);

        //Note -11 = A1234AK; -5 = A1234AE, -16 not in caseload
        assertThatOKResponseContainsJson(responseEntity, """
                [{"offenderNo":"A1234AK"},{"offenderNo":"A1234AE"}]
            """);
    }

    @Test
    public void postOffenderSentenceBookings_success_multiple() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"),
            List.of("-58", "-34", "-33", "-32", "-31", "-30", "-29", "-28", "-27", "-25", "-24", "-19", "-18", "-17", "-12", "-11", "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1", "16048"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences.json");
    }

    @Test
    public void postOffenderSentenceBookings_unknownUser_noRoles_singleOffender() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("UNKNOWN_USER", List.of(), List.of("-8"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonAndStatus(responseEntity, HTTP_OK, "[]");
    }

    @Test
    public void postOffenderSentenceBookings_unknownUser_viewRole_singleOffender() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("UNKNOWN_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of("-8"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json");
    }

    @Test
    public void postOffenderSentenceBookings_noRoles_singleOffender() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), List.of("-8"));
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "offender_sentences_single.json");
    }

    @Test
    public void postOffenderSentenceBookings_noOffenders() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), List.of());
        final var responseEntity = testRestTemplate.exchange("/api/offender-sentences/bookings", HttpMethod.POST, requestEntity, String.class);

        assertThatJsonAndStatus(responseEntity, 400, """
                    {"status":400,"userMessage":"List of Offender Ids must be provided","developerMessage":"400 List of Offender Ids must be provided"}
            """);
    }

    @Test
    public void getOffenderSentencesWithOffenceInformation() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("RO_USER", List.of("ROLE_VIEW_PRISONER_DATA"), Map.of());

        final var response = testRestTemplate.exchange("/api/offender-sentences/booking/-20/sentences-and-offences",
            HttpMethod.GET,
            requestEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "sentences-and-offences-details.json");
    }

    @Test
    public void postHdcLatestStatus_success_multiple() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "RO_USER",
            List.of("ROLE_SYSTEM_USER"),
            List.of(-1L, -2L, -3L)
        );

        var responseType = new ParameterizedTypeReference<List<HomeDetentionCurfew>>() {};

        final var responseEntity = testRestTemplate
            .exchange(
                "/api/offender-sentences/home-detention-curfews/latest",
                HttpMethod.POST,
                requestEntity,
                responseType
            );

        assertThatStatus(responseEntity, 200);
        assertThat(responseEntity.getBody()).isNotEmpty();
        assertThat(responseEntity.getBody()).hasSize(3);
        assertThat(responseEntity.getBody()).extracting("bookingId").containsOnly(-1L, -2L, -3L);
    }
}
