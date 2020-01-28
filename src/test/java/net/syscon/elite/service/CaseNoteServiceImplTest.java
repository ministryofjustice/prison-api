package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.transformers.CaseNoteTransformer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private BookingService bookingService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    private CaseNoteService caseNoteService;

    @Before
    public void setUp() {
        caseNoteService = new CaseNoteService(repository, new CaseNoteTransformer(userService, null), userService, authenticationFacade, bookingService, 10);
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
                        .text("Hello")
                        .type("KA")
                        .subType("KS")
                        .authorName("Mr Black")
                        .staffId(1L)
                        .build()));

        when(userService.getUserByUsername("staff2"))
                .thenReturn(UserDetail
                        .builder()
                        .staffId(2L)
                        .username("TEST_USER")
                        .build());
        when(authenticationFacade.isOverrideRole("CASE_NOTE_ADMIN")).thenReturn(true);

        when(repository.getCaseNote(1L, 1L))
                .thenReturn(Optional.of(CaseNote
                        .builder()
                        .agencyId("LEI")
                        .bookingId(1L)
                        .caseNoteId(1L)
                        .originalNoteText("Hello")
                        .text("Hello")
                        .amendments(List.of(CaseNoteAmendment.builder().additionalNoteText("update text").build()))
                        .type("KA")
                        .subType("KS")
                        .authorName("Mr Black")
                        .staffId(1L)
                        .build()));

        caseNoteService.updateCaseNote(1L, 1L, "staff2", "update text");

        verify(repository).updateCaseNote(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    public void getCaseNotesEvents_noLimit() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        when(repository.getCaseNoteEvents(any(), anySet(), anyLong())).thenReturn(List.of(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")));
        final var events = caseNoteService.getCaseNotesEvents(List.of("BOB+JOE", "BOB+HARRY", "FRED"), fromDate);

        assertThat(events).containsExactly(bobJoeEvent, fredEvent);
        verify(repository).getCaseNoteEvents(fromDate, Set.of("BOB", "FRED"), Long.MAX_VALUE);
    }

    @Test
    public void getCaseNotesEvents() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        final var bobHarryEvent = createEvent("BOB", "HARRY");
        when(repository.getCaseNoteEvents(any(), anySet(), anyLong())).thenReturn(List.of(bobJoeEvent, bobHarryEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")));
        final var events = caseNoteService.getCaseNotesEvents(List.of("BOB+JOE", "BOB+HARRY", "FRED"), fromDate, 10L);

        assertThat(events).containsExactly(bobJoeEvent, bobHarryEvent, fredEvent);
        verify(repository).getCaseNoteEvents(fromDate, Set.of("BOB", "FRED"), 10L);
    }

    @Test
    public void getCaseNotesEvents_testTrimAndSeparation() {
        final var fromDate = LocalDateTime.now();
        final var fredEvent = createEvent("FRED", "JOE");
        final var bobJoeEvent = createEvent("BOB", "JOE");
        when(repository.getCaseNoteEvents(any(), anySet(), anyLong())).thenReturn(List.of(bobJoeEvent, fredEvent));
        final var events = caseNoteService.getCaseNotesEvents(List.of("BOB+JOE", "   FRED JOE  "), fromDate, 20L);
        assertThat(events).containsExactly(bobJoeEvent, fredEvent);
    }

    private CaseNoteEvent createEvent(final String type, final String subType) {
        return CaseNoteEvent.builder().mainNoteType(type).subNoteType(subType).build();
    }
}
