package net.syscon.elite.executablespecification.steps;

import cucumber.api.Format;
import lombok.Builder;
import lombok.Data;
import net.syscon.elite.api.model.Adjudication;
import net.syscon.elite.api.model.AdjudicationCharge;
import net.syscon.elite.api.model.AdjudicationSearchResponse;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Offender Adjudication feature.
 */
public class OffenderAdjudicationSteps extends CommonSteps {

    private List<Adjudication> adjudications;

    @Step("Perform offender adjudication search")
    public void findAdjudications(final String offenderNumber) {

        init();

        try {

            final var responseEntity = restTemplate.exchange(API_PREFIX + "offenders/{offenderNumber}/adjudications",
                    HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()),
                    new ParameterizedTypeReference<AdjudicationSearchResponse>() {
                    }, offenderNumber);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            adjudications = responseEntity.getBody().getResults();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }


    public void verifyAdjudications(@Format("yyyy-MM-dd HH:mm") final List<AdjudicationRow> expected) {
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

    private String commaSeparated(final Adjudication adjudication, final Function<AdjudicationCharge, String> extractor) {
        return adjudication.getAdjudicationCharges().stream().map(extractor).collect(joining(","));
    }

    @Data
    @Builder
    public static class AdjudicationRow {
        private long adjudicationNumber;
        private LocalDateTime reportDate;
        private String agencyId;
        private String offenceCodes;
        private String findings;
    }
}
