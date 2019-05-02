package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CaseNoteUsageByBookingId;
import net.syscon.elite.api.resource.CaseNoteResource;
import net.syscon.elite.service.CaseNoteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteResourceImplTest {
    @Mock
    private CaseNoteService caseNoteService;
    private CaseNoteResource caseNoteResource;

    @Before
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
