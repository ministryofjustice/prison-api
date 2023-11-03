package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerCaseNoteTypeAndSubType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;


public class CaseNoteResourceIntTest extends ResourceTest {
    @MockBean
    private OffenderCaseNoteRepository offenderCaseNoteRepository;

    @Test
    public void getCaseNoteByTypesAndDates() {
        final var fromDate1 = LocalDateTime.of(2018,2,3,12,0,0);
        final var fromDate2 = LocalDateTime.of(2019,2,3,12,0,0);
        final var fromDate3 = LocalDateTime.of(2020,2,3,12,0,0);

        final var bookingDatePairs = List.of(
            BookingFromDatePair.builder().bookingId(-16L).fromDate(fromDate1).build(),
            BookingFromDatePair.builder().bookingId(-17L).fromDate(fromDate2).build(),
            BookingFromDatePair.builder().bookingId(-18L).fromDate(fromDate3).build()
        );

        when(offenderCaseNoteRepository.findCaseNoteTypesByBookingAndDate(anyList(), anyList(), any(LocalDate.class))).thenReturn (
            List.of(
                new PrisonerCaseNoteTypeAndSubType(-16L, "POS", "IEP_ENC", fromDate1.minusDays(1)),
                new PrisonerCaseNoteTypeAndSubType(-16L, "POS", "IEP_ENC", fromDate1.plusDays(1)),
                new PrisonerCaseNoteTypeAndSubType(-16L, "POS", "IEP_ENC", fromDate1.plusDays(2)),
                new PrisonerCaseNoteTypeAndSubType(-16L, "NEG", "IEP_WARN", fromDate1),
                new PrisonerCaseNoteTypeAndSubType(-16L, "NEG", "IEP_WARN", fromDate1.plusDays(1)),
                new PrisonerCaseNoteTypeAndSubType(-16L, "NEG", "IEP_WARN", fromDate1.plusDays(2)),

                new PrisonerCaseNoteTypeAndSubType(-17L, "POS", "IEP_ENC", fromDate2.minusDays(5)),
                new PrisonerCaseNoteTypeAndSubType(-17L, "POS", "IEP_ENC", fromDate2.minusDays(1)),
                new PrisonerCaseNoteTypeAndSubType(-17L, "POS", "IEP_ENC", fromDate2.plusDays(2)),
                new PrisonerCaseNoteTypeAndSubType(-17L, "NEG", "IEP_WARN", fromDate2.minusDays(5)),
                new PrisonerCaseNoteTypeAndSubType(-17L, "NEG", "IEP_WARN", fromDate2.plusDays(1)),
                new PrisonerCaseNoteTypeAndSubType(-17L, "NEG", "IEP_WARN", fromDate2.plusDays(2)),

                new PrisonerCaseNoteTypeAndSubType(-18L, "POS", "IEP_ENC", fromDate3.minusDays(5)),
                new PrisonerCaseNoteTypeAndSubType(-18L, "POS", "IEP_ENC", fromDate3.minusDays(1)),
                new PrisonerCaseNoteTypeAndSubType(-18L, "POS", "IEP_ENC", fromDate3.plusDays(2)),
                new PrisonerCaseNoteTypeAndSubType(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(5)),
                new PrisonerCaseNoteTypeAndSubType(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(1)),
                new PrisonerCaseNoteTypeAndSubType(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(2))
            )
        );

        final var types = List.of("POS", "NEG");

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("VIEW_CASE_NOTES"), CaseNoteTypeSummaryRequest.builder()
            .types(types)
            .bookingFromDateSelection(bookingDatePairs)
            .build());

        final var responseEntity = testRestTemplate.exchange("/api/case-notes/usage-by-types", HttpMethod.POST,
            requestEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(responseEntity, 200, "case_note_usage_by_type.json");
    }
}
