package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.adjudications.OffenderAdjudicationHearing;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class OffenderResourceImplIntTest_getAdjudications extends ResourceTest {

    @Test
    public void shouldReturnListOfAdjudicationsForUserWithCaseload() {

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/adjudications",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of()),
            new ParameterizedTypeReference<String>() {
            });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathArrayValue("results").isNotEmpty();
        assertThat(json).extractingJsonPathArrayValue("offences").isNotEmpty();
        assertThat(json).extractingJsonPathArrayValue("agencies").isNotEmpty();
    }

    @Test
    public void shouldReturn404WhenNoPrivileges() {
        // run with user that doesn't have access to the caseload

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/adjudications",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of(), Map.of()), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnListOfAdjudicationsForViewAdjudicationsRole() {

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/adjudications",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of("ROLE_VIEW_ADJUDICATIONS"), Map.of()),
            new ParameterizedTypeReference<String>() {
            }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathArrayValue("results").isNotEmpty();
    }

    @Test
    public void shouldReturnOffenderAdjudicationHearingsWhenMatch() {
        webTestClient.post().uri("/api/offenders/adjudication-hearings?agencyId=LEI&fromDate=2015-01-02&toDate=2015-01-03")
            .headers(setAuthorisation(List.of("ROLE_VIEW_PRISONER_DATA")))
            .bodyValue(Set.of("A1181HH"))
            .exchange()
            .expectBodyList(ParameterizedTypeReference.forType(OffenderAdjudicationHearing.class))
            .isEqualTo(
                List.of(
                    OffenderAdjudicationHearing.builder()
                        .agencyId("LEI")
                        .offenderNo("A1181HH")
                        .hearingId(-1)
                        .hearingType("Governor's Hearing Adult")
                        .startTime(LocalDateTime.of(2015, 1, 2, 14, 0))
                        .internalLocationId(-1000)
                        .internalLocationDescription("LEI-AABCW-1")
                        .eventStatus("SCH")
                        .build(),
                    OffenderAdjudicationHearing.builder()
                        .agencyId("LEI")
                        .offenderNo("A1181HH")
                        .hearingId(-2)
                        .hearingType("Governor's Hearing Adult")
                        .startTime(LocalDateTime.of(2015, 1, 2, 14, 0))
                        .internalLocationId(-1001)
                        .internalLocationDescription("LEI-A-1-1001")
                        .eventStatus("SCH")
                        .build()
                )
            );
    }

    @Test
    public void shouldReturnNoOffenderAdjudicationHearingsWhenNoMatch() {
        webTestClient.post().uri("/api/offenders/adjudication-hearings?agencyId=LEI&fromDate=2015-01-02&toDate=2015-01-03")
            .headers(setAuthorisation(List.of("ROLE_VIEW_PRISONER_DATA")))
            .bodyValue(Set.of("XXXXXXX"))
            .exchange()
            .expectBodyList(ParameterizedTypeReference.forType(OffenderAdjudicationHearing.class))
            .isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldReturn404WhenInsufficientPrivileges() {
        webTestClient.post().uri("/api/offenders/adjudication-hearings?agencyId=LEI&fromDate=2015-01-02&toDate=2015-01-03")
            .headers(setAuthorisation(List.of()))
            .bodyValue(Set.of("A1181HH"))
            .exchange()
            .expectStatus()
            .isForbidden();
    }
}
