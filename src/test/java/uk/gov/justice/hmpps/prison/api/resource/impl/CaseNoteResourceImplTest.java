package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
public class CaseNoteResourceImplTest {
    @Mock
    private CaseNoteService caseNoteService;
    private CaseNoteResource caseNoteResource;

    @BeforeEach
    public void setUp() {
        caseNoteResource = new CaseNoteResourceImpl(caseNoteService);
    }

    @Test
    public void getCaseNoteUsageByBookingId() {
        final var usage = List.of(new CaseNoteUsageByBookingId(-16, "OBSERVE", "OBS_GEN", 1, LocalDateTime.parse("2017-05-13T12:00")));
        final var bookingIds = List.of(2, 3, 4);
        when(caseNoteService.getCaseNoteUsageByBookingId(anyString(), anyString(), anyList(), any(), any(), anyInt())).thenReturn(usage);
        assertThat(caseNoteResource.getCaseNoteSummaryByBookingId(bookingIds, 2, null, null, "BOB", "SMITH")).isEqualTo(usage);
        verify(caseNoteService).getCaseNoteUsageByBookingId("BOB", "SMITH", bookingIds, null, null, 2);
    }
}
