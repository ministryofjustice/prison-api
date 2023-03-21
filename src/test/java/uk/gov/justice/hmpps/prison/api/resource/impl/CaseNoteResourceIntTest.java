package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CaseNoteResourceIntTest extends ResourceTest {
    @MockBean
    private CaseNoteRepository caseNoteRepository;

    @MockBean
    private OffenderCaseNoteRepository offenderCaseNoteRepository;

    @Test
    public void getCaseNoteEvents_noLimit() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        when(caseNoteRepository.getCaseNoteEvents(any(), anySet(), anyLong())).thenReturn(List.of(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/case-notes/events_no_limit?type=BOB+JOE&type=FRED&createdDate=" + fromDate, HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "casenoteevents.json");

        verify(caseNoteRepository).getCaseNoteEvents(fromDate, Set.of("BOB", "FRED"), Long.MAX_VALUE);
    }

    @Test
    public void getCaseNoteEvents() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        when(caseNoteRepository.getCaseNoteEvents(any(), anySet(), anyLong())).thenReturn(List.of(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/case-notes/events?limit=10&type=BOB+JOE&type=FRED&createdDate=" + fromDate, HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "casenoteevents.json");

        verify(caseNoteRepository).getCaseNoteEvents(fromDate, Set.of("BOB", "FRED"), 10);
    }

    @Test
    public void getCaseNoteEvents_missingLimit() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/case-notes/events?&type=BOB+JOE&type=FRED&createdDate=" + LocalDateTime.now(), HttpMethod.GET, requestEntity, String.class);
        assertThatJsonFileAndStatus(responseEntity, 400, "casenoteevents_validation.json");
    }

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

        when(offenderCaseNoteRepository.findByOffenderBooking_BookingIdInAndType_CodeInAndOccurrenceDateTimeGreaterThanEqual(anyList(), anyList(), any(LocalDateTime.class))).thenReturn(
            List.of(
                buildCaseNote(-16L, "POS", "IEP_ENC", fromDate1.minusDays(1)),
                buildCaseNote(-16L, "POS", "IEP_ENC", fromDate1.plusDays(1)),
                buildCaseNote(-16L, "POS", "IEP_ENC", fromDate1.plusDays(2)),
                buildCaseNote(-16L, "NEG", "IEP_WARN", fromDate1),
                buildCaseNote(-16L, "NEG", "IEP_WARN", fromDate1.plusDays(1)),
                buildCaseNote(-16L, "NEG", "IEP_WARN", fromDate1.plusDays(2)),

                buildCaseNote(-17L, "POS", "IEP_ENC", fromDate2.minusDays(5)),
                buildCaseNote(-17L, "POS", "IEP_ENC", fromDate2.minusDays(1)),
                buildCaseNote(-17L, "POS", "IEP_ENC", fromDate2.plusDays(2)),
                buildCaseNote(-17L, "NEG", "IEP_WARN", fromDate2.minusDays(5)),
                buildCaseNote(-17L, "NEG", "IEP_WARN", fromDate2.plusDays(1)),
                buildCaseNote(-17L, "NEG", "IEP_WARN", fromDate2.plusDays(2)),

                buildCaseNote(-18L, "POS", "IEP_ENC", fromDate3.minusDays(5)),
                buildCaseNote(-18L, "POS", "IEP_ENC", fromDate3.minusDays(1)),
                buildCaseNote(-18L, "POS", "IEP_ENC", fromDate3.plusDays(2)),
                buildCaseNote(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(5)),
                buildCaseNote(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(1)),
                buildCaseNote(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(2))

            )
        );

        final var types = List.of("POS", "NEG");

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), CaseNoteTypeSummaryRequest.builder()
            .types(types)
            .bookingFromDateSelection(bookingDatePairs)
            .build());

        final var responseEntity = testRestTemplate.exchange("/api/case-notes/usage-by-types", HttpMethod.POST,
            requestEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(responseEntity, 200, "case_note_usage_by_type.json");
    }

    private static OffenderCaseNote buildCaseNote(long bookingId, String type, String subType, LocalDateTime occurrenceDateTime) {
        return OffenderCaseNote.builder()
            .offenderBooking(OffenderBooking.builder().bookingId(bookingId).build())
            .type(new CaseNoteType(type, null))
            .subType(new CaseNoteSubType(subType, null))
            .occurrenceDateTime(occurrenceDateTime)
            .build();
    }

    private CaseNoteEvent createEvent(final String type, final String subType) {
        return CaseNoteEvent.builder()
                .mainNoteType(type)
                .subNoteType(subType)
                .content("Some content for " + subType)
                .contactTimestamp(LocalDateTime.parse("2019-02-01T23:22:21"))
                .notificationTimestamp(LocalDateTime.parse("2019-02-01T23:22:21"))
                .establishmentCode("LEI")
                .firstName("FIRST")
                .lastName("LAST")
                .id(1L)
                .nomsId(123 + subType)
                .build();
    }
}
