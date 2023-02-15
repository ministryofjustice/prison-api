package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CaseNoteResourceIntTest extends ResourceTest {
    @MockBean
    private CaseNoteRepository caseNoteRepository;

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

        final var dbResults = List.of(
            new CaseNoteUsageByBookingId(-16, "POS", "IEP_ENC", 2, LocalDateTime.parse("2017-05-13T12:00")),
            new CaseNoteUsageByBookingId(-16, "NEG", "IEP_WARN", 3, LocalDateTime.parse("2018-05-13T12:00")),
            new CaseNoteUsageByBookingId(-17, "POS", "IEP_ENC", 1, LocalDateTime.parse("2018-05-13T12:00")),
            new CaseNoteUsageByBookingId(-17, "NEG", "IEP_WARN", 2, LocalDateTime.parse("2018-05-13T12:00")),
            new CaseNoteUsageByBookingId(-18, "POS", "IEP_ENC", 1, LocalDateTime.parse("2018-05-13T12:00"))
        );
        final var bookingDatePairs = List.of(
            BookingFromDatePair.builder().bookingId(-16).fromDate(fromDate1).build(),
            BookingFromDatePair.builder().bookingId(-17).fromDate(fromDate2).build(),
            BookingFromDatePair.builder().bookingId(-18).fromDate(fromDate3).build()
        );

        final var types = List.of("POS", "NEG");
        when(caseNoteRepository.getCaseNoteUsageByBookingIdAndFromDate(anyList(), anyInt(), any())).thenReturn(dbResults);

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of(), CaseNoteTypeSummaryRequest.builder()
            .types(types)
            .bookingFromDateSelection(bookingDatePairs)
            .build());

        final var responseEntity = testRestTemplate.exchange("/api/case-notes/usage-by-types", HttpMethod.POST,
            requestEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(responseEntity, 200, "case_note_usage_by_type.json");

        verify(caseNoteRepository).getCaseNoteUsageByBookingIdAndFromDate(types, -16, fromDate1);
        verify(caseNoteRepository).getCaseNoteUsageByBookingIdAndFromDate(types, -17, fromDate2);
        verify(caseNoteRepository).getCaseNoteUsageByBookingIdAndFromDate(types, -18, fromDate3);
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
