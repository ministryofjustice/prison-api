package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeCount;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.resource.CaseNoteResource;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseNoteResourceTest {
    @Mock
    private CaseNoteService caseNoteService;
    private CaseNoteResource caseNoteResource;

    @BeforeEach
    public void setUp() {
        caseNoteResource = new CaseNoteResource(caseNoteService);
    }

    @Test
    public void getCaseNoteUsageByBookingId() {
        final var usage = List.of(new CaseNoteUsageByBookingId(-16L, "OBSERVE", "OBS_GEN", 1L, LocalDateTime.parse("2017-05-13T12:00")));
        final var bookingIds = List.of(2L, 3L, 4L);
        when(caseNoteService.getCaseNoteUsageByBookingId(anyString(), anyString(), anyList(), any(), any(), anyInt())).thenReturn(usage);
        assertThat(caseNoteResource.getCaseNoteSummaryByBookingId(bookingIds, 2, null, null, "BOB", "SMITH")).isEqualTo(usage);
        verify(caseNoteService).getCaseNoteUsageByBookingId("BOB", "SMITH", bookingIds, null, null, 2);
    }

    @Test
    public void getCaseNoteUsageByBookingIdTypeAndDate() {
        final var usage = List.of(
            new CaseNoteTypeCount(-16L, "POS", "IEP_ENC", 2L),
            new CaseNoteTypeCount(-16L, "NEG", "IEP_WARN", 3L),
            new CaseNoteTypeCount(-17L, "POS", "IEP_ENC", 1L)
        );
        when(caseNoteService.getCaseNoteUsageByBookingIdTypeAndDate(anyList(), anyList())).thenReturn(usage);
        final var bookingDatePairs = List.of(
            BookingFromDatePair.builder().bookingId(-16L).fromDate(LocalDateTime.parse("2017-05-13T12:00")).build(),
            BookingFromDatePair.builder().bookingId(-17L).fromDate(LocalDateTime.parse("2018-05-13T12:00")).build()
        );
        final var types = List.of("POS", "NEG");
        assertThat(caseNoteResource.getCaseNoteUsageSummaryByDates(CaseNoteTypeSummaryRequest.builder()
            .types(types)
            .bookingFromDateSelection(bookingDatePairs)
            .build())).isEqualTo(usage);
        verify(caseNoteService).getCaseNoteUsageByBookingIdTypeAndDate(types, bookingDatePairs);
    }
}
