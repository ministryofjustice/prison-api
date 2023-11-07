package uk.gov.justice.hmpps.prison.executablespecification.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationCharge;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSearchResponse;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Hearing;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Offender Adjudication feature.
 */
public class OffenderAdjudicationSteps extends CommonSteps {

    private AdjudicationDetail detail;
    private List<Adjudication> adjudications;
    private List<AdjudicationOffence> offences;
    private List<Agency> agencies;

    @Step("Perform offender adjudication search")
    public void findAdjudications(final String offenderNumber, final Map<String, String> params) {

        init();

        try {
            var queryParams = new LinkedMultiValueMap<String, String>();
            params.forEach(queryParams::add);

            URI uri = UriComponentsBuilder.fromPath(API_PREFIX)
                    .path("offenders/{offenderNumber}/adjudications")
                    .queryParams(queryParams)
                    .build(offenderNumber);

            final var responseEntity = restTemplate.exchange(uri,
                    HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()),
                    ParameterizedTypeReference.<AdjudicationSearchResponse>forType(AdjudicationSearchResponse.class));

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            AdjudicationSearchResponse body = responseEntity.getBody();
            adjudications = body.getResults();
            offences = body.getOffences();
            agencies = body.getAgencies();

        } catch (PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Retrieve adjudication detail")
    public void findAdjudicationDetails(final String offenderNumber, final String adjudicationNo) {

        init();

        try {

            URI uri = UriComponentsBuilder.fromPath(API_PREFIX)
                    .path("offenders/{offenderNumber}/adjudications/{adjudicationNumber}")
                    .build(offenderNumber, adjudicationNo);

            final var responseEntity = restTemplate.exchange(uri,
                    HttpMethod.GET,
                    createEntity(null),
                    AdjudicationDetail.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            detail = responseEntity.getBody();


        } catch (PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }


    public void verifyAdjudications(final List<AdjudicationRow> expected) {
        final var found = adjudications.stream()
            .map(adj -> AdjudicationRow.builder().
                adjudicationNumber(adj.getAdjudicationNumber()).
                reportDate(adj.getReportTime()).
                agencyId(adj.getAgencyId()).
                offenceCodes(commaSeparated(adj, AdjudicationCharge::getOffenceCode)).
                findings(commaSeparated(adj, AdjudicationCharge::getFindingCode))
                .build())
            .collect(toList());

        assertThat(found).containsExactlyElementsOf(expected);
    }

    public void verifyOffenceCodes(final List<String> expectedChargeCodes) {
        final var found = offences.stream().map(AdjudicationOffence::getCode).collect(toSet());
        assertThat(found).containsExactlyInAnyOrderElementsOf(Set.copyOf(expectedChargeCodes));
    }

    public void verifyAgencies(List<String> expectedAgencyIds) {
        final var found = agencies.stream().map(Agency::getAgencyId).collect(toSet());
        assertThat(found).containsExactlyInAnyOrderElementsOf(Set.copyOf(expectedAgencyIds));
    }

    public void verifyAdjudicationDetails() {
        verifyNoError();
        assertThat(detail).isNotNull();
        assertThat(detail.getAdjudicationNumber()).isEqualTo(-7);

        List<Hearing> hearings = detail.getHearings();
        assertThat(hearings).extracting(Hearing::getOicHearingId).containsExactly(-1L, -2L);
    }

    private String commaSeparated(final Adjudication adjudication, final Function<AdjudicationCharge, String> extractor) {
        return adjudication.getAdjudicationCharges().stream().map(extractor).collect(joining(","));
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdjudicationRow {
        private long adjudicationNumber;
        private LocalDateTime reportDate;
        private String agencyId;
        private String offenceCodes;
        private String findings;
    }
}
