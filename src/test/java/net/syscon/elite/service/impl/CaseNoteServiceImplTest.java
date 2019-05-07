package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseNoteUsageByBookingId;
import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteServiceImplTest {
    @Mock
    private CaseNoteRepository repository;
    @Mock
    private UserService userService;

    private CaseNoteService caseNoteService;

    @Before
    public void setUp() {
        caseNoteService = new CaseNoteServiceImpl(repository, new CaseNoteTransformer(userService, null), userService, null, 10);
    }

    @Test
    public void getCaseNoteUsageByBookingId() {
        final var usage = List.of(new CaseNoteUsageByBookingId(-16, "OBSERVE", "OBS_GEN", 1, LocalDateTime.parse("2017-05-13T12:00")));
        when(repository.getCaseNoteUsageByBookingId(anyString(), anyString(), anyList(), any(), any())).thenReturn(usage);

        final var bookingIds = List.of(2, 3, 4);
        assertThat(caseNoteService.getCaseNoteUsageByBookingId("TYPE", "SUBTYPE", bookingIds, null, null, 3)).isEqualTo(usage);

        final var tomorrow = LocalDate.now().plusDays(1);
        final var threeMonthsAgo = LocalDate.now().minusMonths(3);

        verify(repository).getCaseNoteUsageByBookingId("TYPE", "SUBTYPE", bookingIds, threeMonthsAgo, tomorrow);
    }
}
