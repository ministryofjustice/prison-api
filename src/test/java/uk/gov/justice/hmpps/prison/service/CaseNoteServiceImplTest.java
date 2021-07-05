package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteAmendment;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.transformers.CaseNoteTransformer;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSizeValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseNoteServiceImplTest {
    @Mock
    private CaseNoteRepository repository;

    @Mock
    private OffenderCaseNoteRepository offenderCaseNoteRepository;

    @Mock
    private OffenderBookingRepository offenderBookingRepository;
    @Mock
    private StaffUserAccountRepository staffUserAccountRepository;
    @Mock
    private ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository;
    @Mock
    private ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingService bookingService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private MaximumTextSizeValidator maximumTextSizeValidator;

    private CaseNoteService caseNoteService;

    @BeforeEach
    public void setUp() {
        caseNoteService = new CaseNoteService(repository, offenderCaseNoteRepository, new CaseNoteTransformer(userService, null), userService,
            authenticationFacade, bookingService, 10, maximumTextSizeValidator, offenderBookingRepository, staffUserAccountRepository, caseNoteTypeReferenceCodeRepository, caseNoteSubTypeReferenceCodeRepository);
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
    public void testCaseNoteAmendedSizeExceedsMaximum() {
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
        when(maximumTextSizeValidator.isValid(anyString(), any())).thenReturn(true);

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

        when(maximumTextSizeValidator.isValid(anyString(), any())).thenReturn(false);
        when(maximumTextSizeValidator.getMaximumAnsiEncodingSize()).thenReturn(100);

        assertThatThrownBy(() -> caseNoteService.updateCaseNote(1L, 1L, "staff2", "update text"))
            .isInstanceOf(org.springframework.web.client.HttpClientErrorException.class)
            .hasMessageContaining("Length should not exceed 31 characters");

        verify(maximumTextSizeValidator).isValid(ArgumentMatchers.contains("update text"), ArgumentMatchers.isNull());

    }

    @Test
    public void testThatTheCaseNoteAmendmentRestrictions_AreIgnoredGivenTheCorrectRole() {
        when(offenderCaseNoteRepository.findByIdAndOffenderBooking_BookingId(1L, 1L))
                .thenReturn(Optional.of(OffenderCaseNote
                        .builder()
                        .agencyLocation(AgencyLocation.builder().id("LEI").build())
                        .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
                        .id(1L)
                        .caseNoteText("Hello")
                        .type(new CaseNoteType("KA", "Keyworker"))
                        .subType(new CaseNoteSubType("KS", "Keyworker Session"))
                        .author(Staff.builder().staffId(1L).firstName("Ted").lastName("Black").build())
                        .build()));

        when(userService.getUserByUsername("staff2"))
                .thenReturn(UserDetail
                        .builder()
                        .staffId(2L)
                        .username("TEST_USER")
                        .build());
        when(authenticationFacade.isOverrideRole("CASE_NOTE_ADMIN")).thenReturn(true);
        when(maximumTextSizeValidator.isValid(anyString(), any())).thenReturn(true);

        when(offenderCaseNoteRepository.findByIdAndOffenderBooking_BookingId(1L, 1L))
                .thenReturn(Optional.of(OffenderCaseNote
                    .builder()
                    .agencyLocation(AgencyLocation.builder().id("LEI").build())
                    .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
                    .id(1L)
                    .caseNoteText(String.format("%s ...[%s updated the case notes on %s] %s", "Hello", "staff2", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")), "update text"))
                    .type(new CaseNoteType("KA", "Keyworker"))
                    .subType(new CaseNoteSubType("KS", "Keyworker Session"))
                    .author(Staff.builder().staffId(1L).firstName("Ted").lastName("Black").build())
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
