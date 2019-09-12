package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.CaseNoteEvent;
import net.syscon.elite.api.model.CaseNoteUsageByBookingId;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.security.AuthenticationFacade;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteServiceImplTest {
    @Mock
    private CaseNoteRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    private CaseNoteService caseNoteService;

    @Before
    public void setUp() {
        caseNoteService = new CaseNoteServiceImpl(repository, new CaseNoteTransformer(userService, null), userService, null, authenticationFacade, 10);
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

    @Test
    public void testCaseNoteAmendmentRestriction() {
        when(repository.getCaseNote(1L, 1L))
                .thenReturn(Optional.of(CaseNote
                        .builder()
                        .agencyId("LEI")
                        .bookingId(1L)
                        .caseNoteId(1L)
                        .originalNoteText("Hello")
                        .staffId(1L)
                        .build()));

        when(userService.getUserByUsername("staff2"))
                .thenReturn(UserDetail
                        .builder()
                        .staffId(2L)
                        .build());

        assertThatThrownBy(() -> caseNoteService.updateCaseNote(1L, 1L, "staff2", "update text"))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

    }

    @Test
    public void testThatTheCaseNoteAmendmentRestrictions_AreIgnoredGivenTheCorrectRole() {
        when(repository.getCaseNote(1L, 1L))
                .thenReturn(Optional.of(CaseNote
                        .builder()
                        .agencyId("LEI")
                        .bookingId(1L)
                        .caseNoteId(1L)
                        .originalNoteText("Hello")
                        .staffId(1L)
                        .build()));

        when(userService.getUserByUsername("staff2"))
                .thenReturn(UserDetail
                        .builder()
                        .staffId(2L)
                        .build());

        when(authenticationFacade.isOverrideRole("CASE_NOTE_ADMIN")).thenReturn(true);

        caseNoteService.updateCaseNote(1L, 1L, "staff2", "update text");

        verify(repository).updateCaseNote(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    public void getCaseNotesEvents() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        when(repository.getCaseNoteEvents(any())).thenReturn(List.of(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")));
        final var events = caseNoteService.getCaseNotesEvents(List.of("BOB+JOE", "FRED"), fromDate);
        assertThat(events).containsExactly(bobJoeEvent, fredEvent);
    }

    private CaseNoteEvent createEvent(final String type, final String subType) {
        return CaseNoteEvent.builder().mainNoteType(type).subNoteType(subType).build();
    }
}
