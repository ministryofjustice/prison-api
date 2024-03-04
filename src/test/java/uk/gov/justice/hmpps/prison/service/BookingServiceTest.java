package uk.gov.justice.hmpps.prison.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.BookingActivity;
import uk.gov.justice.hmpps.prison.api.model.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecord;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderOffence;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetailDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerm;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentDetail;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalculationSummary;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.VisitWithVisitors;
import uk.gov.justice.hmpps.prison.api.model.Visitor;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.repository.SentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtOrder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventOutcome;
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceIndicator;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderContactPerson;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFinePayment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Person;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalcType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitVisitor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderChargeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderContactPersonsRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderFinePaymentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRestrictionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceTermRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitVisitorRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitorRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderBookingTransformer;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderChargeTransformer;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link BookingService}.
 */
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CourtEventRepository courtEventRepository;
    @Mock
    private OffenderBookingRepository offenderBookingRepository;
    @Mock
    private OffenderChargeRepository offenderChargeRepository;
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private VisitInformationRepository visitInformationRepository;
    @Mock
    private VisitorRepository visitorRepository;
    @Mock
    private VisitVisitorRepository visitVisitorRepository;
    @Mock
    private AgencyService agencyService;
    @Mock
    private OffenderFixedTermRecallService offenderFixedTermRecallService;
    @Mock
    private OffenderContactPersonsRepository offenderContactPersonsRepository;
    @Mock
    private OffenderRestrictionRepository offenderRestrictionRepository;
    @Mock
    private StaffUserAccountRepository staffUserAccountRepository;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private OffenderTransformer offenderTransformer;
    @Mock
    private OffenderBookingTransformer offenderBookingTransformer;
    @Mock
    private OffenderSentenceRepository offenderSentenceRepository;
    @Mock
    private SentenceRepository sentenceRepository;
    @Mock
    private SentenceTermRepository sentenceTermRepository;
    @Mock
    private OffenderFinePaymentRepository offenderFinePaymentRepository;
    @Mock
    private CaseloadToAgencyMappingService caseloadToAgencyMappingService;
    @Mock
    private CaseLoadService caseLoadService;
    @Mock
    private OffenderChargeTransformer offenderChargeTransformer;

    @Mock
    private AgencyLocationRepository agencyLocationRepository;
    private BookingService bookingService;

    @Mock
    private TelemetryClient telemetryClient;

    @BeforeEach
    public void init() {
        bookingService = new BookingService(
            bookingRepository,
            courtEventRepository,offenderBookingRepository,
            offenderChargeRepository,
            offenderRepository,
            visitorRepository,
            visitInformationRepository,
            visitVisitorRepository,
            sentenceRepository,
            sentenceTermRepository,

            agencyService,offenderFixedTermRecallService,
            caseLoadService,
            caseloadToAgencyMappingService,
            offenderContactPersonsRepository,
            staffUserAccountRepository,
            offenderBookingTransformer,
            offenderTransformer,
            authenticationFacade,
            offenderSentenceRepository,
            offenderFinePaymentRepository,
            offenderRestrictionRepository,
            offenderChargeTransformer,
            agencyLocationRepository,telemetryClient,
            10);
    }

    @Test
    public void testVerifyCanAccessLatestBooking() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds(false)).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true);

        bookingService.getOffenderIdentifiers("off-1", false);
    }

    @Test
    public void testVerifyCannotAccessLatestBooking() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds(false)).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false);

        assertThatThrownBy(() ->
            bookingService.getOffenderIdentifiers("off-1", false))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testVerifyCannotAccessLatestBookingForAccessDenied() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds(false)).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false);

        assertThatThrownBy(() ->
            bookingService.getOffenderIdentifiers("off-1", true))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds(false)).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true);


        bookingService.getOffenderIdentifiers("off-1", false);
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo_systemUser() {
        when(authenticationFacade.isOverrideRole(any(String[].class))).thenReturn(true);

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", -1L, 1)));

        bookingService.getOffenderIdentifiers("off-1", false, "SYSTEM_USER", "GLOBAL_SEARCH");

        verify(authenticationFacade).isOverrideRole(
            "SYSTEM_USER", "GLOBAL_SEARCH"
        );
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo_not() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds(false)).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false);

        assertThatThrownBy(() ->
            bookingService.getOffenderIdentifiers("off-1", false))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testThatUpdateAttendanceIsCalledForEachBooking() {
        when(bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", "ATT"))
            .thenReturn(PayableAttendanceOutcomeDto
                .builder()
                .paid(true)
                .build());

        final var updateAttendance = UpdateAttendance
            .builder()
            .eventOutcome("ATT")
            .performance("STANDARD")
            .build();

        final var bookingActivities = Set.of(
            BookingActivity.builder().bookingId(1L).activityId(10L).build(),
            BookingActivity.builder().bookingId(2L).activityId(20L).build(),
            BookingActivity.builder().bookingId(3L).activityId(30L).build(),
            BookingActivity.builder().bookingId(3L).activityId(31L).build(),
            BookingActivity.builder().bookingId(3L).activityId(32L).build()
        );

        bookingService.updateAttendanceForMultipleBookingIds(bookingActivities, updateAttendance);

        final var expectedOutcome = UpdateAttendance.builder().performance("STANDARD").eventOutcome("ATT").build();

        verify(bookingRepository).updateAttendance(1L, 10L, expectedOutcome, true, false);
        verify(bookingRepository).updateAttendance(2L, 20L, expectedOutcome, true, false);
        verify(bookingRepository).updateAttendance(3L, 30L, expectedOutcome, true, false);
        verify(bookingRepository).updateAttendance(3L, 31L, expectedOutcome, true, false);
        verify(bookingRepository).updateAttendance(3L, 32L, expectedOutcome, true, false);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    public void getBookingVisitBalances() {
        final var bookingId = -1L;
        when(bookingRepository.getBookingVisitBalances(bookingId)).thenReturn(Optional.of(new VisitBalances(25, 2, LocalDate.now(), LocalDate.now())));

        bookingService.getBookingVisitBalances(bookingId);

        verify(bookingRepository).getBookingVisitBalances(bookingId);
    }

    @Nested
    public class getEvents {
        @Test
        public void getEvents_CallsBookingRepository() {
            final var bookingId = -1L;
            final var from = LocalDate.parse("2019-02-03");
            final var to = LocalDate.parse("2019-03-03");
            bookingService.getEvents(bookingId, from, to);
            verify(bookingRepository).getBookingActivities(bookingId, from, to, "startTime", Order.ASC);
            verify(bookingRepository).getBookingVisits(bookingId, from, to, "startTime", Order.ASC);
            verify(bookingRepository).getBookingAppointments(bookingId, from, to, "startTime", Order.ASC);
        }

        @Test
        public void getEvents_SortsAndCombinesNullsLast() {
            final var bookingId = -1L;
            final var from = LocalDate.parse("2019-02-03");
            final var to = LocalDate.parse("2019-03-03");
            when(bookingRepository.getBookingActivities(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("act", "10:11:12"),
                    createEvent("act", "08:59:50"))
            );
            when(bookingRepository.getBookingVisits(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("vis", null))
            );
            when(bookingRepository.getBookingAppointments(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("app", "09:02:03"))
            );
            final var events = bookingService.getEvents(bookingId, from, to);
            assertThat(events).extracting(ScheduledEvent::getEventType).containsExactly("act08:59:50", "app09:02:03", "act10:11:12", "visnull");
        }
    }

    @Nested
    public class getScheduledEvents {
        @Test
        public void getScheduledEvents_CallsBookingRepository() {
            final var bookingId = -1L;
            final var from = LocalDate.now();
            final var to = from.plusDays(1);
            bookingService.getScheduledEvents(bookingId, from, to);
            verify(bookingRepository).getBookingActivities(bookingId, from, to, "startTime", Order.ASC);
            verify(bookingRepository).getBookingVisits(bookingId, from, to, "startTime", Order.ASC);
            verify(bookingRepository).getBookingAppointments(bookingId, from, to, "startTime", Order.ASC);
        }

        @Test
        public void getScheduledEvents_MustBeInFuture() {
            final var bookingId = -1L;
            final var from = LocalDate.parse("2019-05-05");
            final var to = from.plusDays(1);
            assertThatThrownBy(() -> bookingService.getScheduledEvents(bookingId, from, to))
                .isInstanceOf(HttpClientErrorException.class).hasMessage("400 Invalid date range: fromDate is before today.");
        }

        @Test
        public void getScheduledEvents_DefaultsToToday() {
            final var bookingId = -1L;
            bookingService.getScheduledEvents(bookingId, null, null);
            verify(bookingRepository).getBookingActivities(bookingId, LocalDate.now(), LocalDate.now(), "startTime", Order.ASC);
        }

        @Test
        public void getScheduledEvents_FiltersOutNonScheduledEvents() {
            final var bookingId = -1L;
            when(bookingRepository.getBookingActivities(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(
                    ScheduledEvent.builder().bookingId(-1L).eventType("act").eventStatus("SCH").build(),
                    ScheduledEvent.builder().bookingId(-2L).eventType("act").eventStatus("CANC").build()
                )
            );
            when(bookingRepository.getBookingVisits(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(
                    ScheduledEvent.builder().bookingId(-3L).eventType("vis").eventStatus("SCH").build(),
                    ScheduledEvent.builder().bookingId(-4L).eventType("vis").eventStatus("EXP").build()
                )
            );
            when(bookingRepository.getBookingAppointments(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(
                    ScheduledEvent.builder().bookingId(-5L).eventType("app").eventStatus("SCH").build(),
                    ScheduledEvent.builder().bookingId(-6L).eventType("app").eventStatus("CANC").build()
                )
            );
            final var events = bookingService.getScheduledEvents(bookingId, null, null);
            assertThat(events).extracting(ScheduledEvent::getEventStatus).containsOnly("SCH");
        }

        @Test
        public void getScheduledEvents_SortsAndCombinesNullsLast() {
            final var bookingId = -1L;
            final var from = LocalDate.now();
            when(bookingRepository.getBookingActivities(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("act", "10:11:12"),
                    createEvent("act", "08:59:50"))
            );
            when(bookingRepository.getBookingVisits(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("vis", null))
            );
            when(bookingRepository.getBookingAppointments(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("app", "09:02:03"))
            );
            final var events = bookingService.getScheduledEvents(bookingId, from, null);
            assertThat(events).extracting(ScheduledEvent::getEventType).containsExactly("act08:59:50", "app09:02:03", "act10:11:12", "visnull");
        }
    }

    @Test
    public void getMilitaryRecords_map() {
        when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OffenderBooking.builder()
            .militaryRecords(List.of(
                OffenderMilitaryRecord.builder()
                    .startDate(LocalDate.parse("2000-01-01"))
                    .endDate(LocalDate.parse("2020-10-17"))
                    .militaryDischarge(new MilitaryDischarge("DIS", "Dishonourable"))
                    .warZone(new WarZone("AFG", "Afghanistan"))
                    .militaryBranch(new MilitaryBranch("ARM", "Army"))
                    .description("left")
                    .unitNumber("auno")
                    .enlistmentLocation("Somewhere")
                    .militaryRank(new MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
                    .serviceNumber("asno")
                    .disciplinaryAction(new DisciplinaryAction("CM", "Court Martial"))
                    .dischargeLocation("Sheffield")
                    .build(),
                OffenderMilitaryRecord.builder()
                    .startDate(LocalDate.parse("2001-01-01"))
                    .militaryBranch(new MilitaryBranch("NAV", "Navy"))
                    .description("second record")
                    .build()))
            .build()));
        final var militaryRecords = bookingService.getMilitaryRecords(-1L);
        assertThat(militaryRecords).usingRecursiveComparison().isEqualTo(new MilitaryRecords(List.of(
            MilitaryRecord.builder()
                .startDate(LocalDate.parse("2000-01-01"))
                .endDate(LocalDate.parse("2020-10-17"))
                .militaryDischargeCode("DIS")
                .militaryDischargeDescription("Dishonourable")
                .warZoneCode("AFG")
                .warZoneDescription("Afghanistan")
                .militaryBranchCode("ARM")
                .militaryBranchDescription("Army")
                .description("left")
                .unitNumber("auno")
                .enlistmentLocation("Somewhere")
                .militaryRankCode("LCPL_RMA")
                .militaryRankDescription("Lance Corporal  (Royal Marines)")
                .serviceNumber("asno")
                .disciplinaryActionCode("CM")
                .disciplinaryActionDescription("Court Martial")
                .dischargeLocation("Sheffield")
                .build(),
            MilitaryRecord.builder()
                .startDate(LocalDate.parse("2001-01-01"))
                .militaryBranchCode("NAV")
                .militaryBranchDescription("Navy")
                .description("second record")
                .build())));
    }

    @Test
    public void getMilitaryRecords_notfound() {
        when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.getMilitaryRecords(-1L)).isInstanceOf(EntityNotFoundException.class).hasMessage("Offender booking with id -1 not found.");
    }

    @Test
    public void getBookingVisitsWithVisitor() {
        Pageable pageable = PageRequest.of(0, 20);
        var visits = List.of(VisitInformation
            .builder()
            .visitId(-1L)
            .visitorPersonId(-1L)
            .cancellationReason(null)
            .cancelReasonDescription(null)
            .eventStatus("ATT")
            .eventStatusDescription("Attended")
            .eventOutcome("ATT")
            .eventOutcomeDescription("Attended")
            .startTime(LocalDateTime.parse("2019-10-10T14:00"))
            .endTime(LocalDateTime.parse("2019-10-10T15:00"))
            .location("Visits")
            .visitType("SOC")
            .visitTypeDescription("Social")
            .leadVisitor("John Smith")
            .searchType("FULL")
            .searchTypeDescription("Full Search")
            .build());

        var page = new PageImpl<>(visits);
        when(offenderContactPersonsRepository.findAllByOffenderBooking_BookingIdAndPersonIdIn(-1L, List.of(-1L, -2L))).thenReturn(List.of(
            OffenderContactPerson.builder()
                .relationshipType(new RelationshipType("UN", "Uncle"))
                .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                .createDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                .personId(-1L)
                .id(-1L)
                .build(),
            OffenderContactPerson.builder()
                .relationshipType(new RelationshipType("FRI", "Friend"))
                .modifyDateTime(null)
                .createDateTime(LocalDateTime.parse("2019-10-11T14:00"))
                .personId(-1L)
                .id(-2L)
                .build(),
            OffenderContactPerson.builder()
                .relationshipType(new RelationshipType("NIE", "Niece"))
                .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                .id(-3L)
                .personId(-2L)
                .build()

        ));
        when(visitInformationRepository.findAll(VisitInformationFilter.builder().bookingId(-1L).build(), pageable))
            .thenReturn(page);

        when(visitVisitorRepository.findByVisitIdInAndOffenderBookingIsNullOrderByPerson_BirthDateDesc(List.of(-1L))).thenReturn(List.of(
            VisitVisitor.builder()
                .visitId(-1L)
                .person(Person.builder()
                    .id(-1L)
                    .birthDate(LocalDate.parse("1980-10-01"))
                    .firstName("John")
                    .lastName("Smith")
                    .build())
                .groupLeader(true)
                .eventOutcome(new EventOutcome("ABS", "Absent"))
                .build(),
            VisitVisitor.builder()
                .visitId(-1L)
                .person(Person.builder()
                    .id(-2L)
                    .birthDate(LocalDate.parse("2010-10-01"))
                    .firstName("Jenny")
                    .lastName("Smith")
                    .build())
                .groupLeader(false)
                .eventOutcome(new EventOutcome("ATT", "Attended"))
                .build()

        ));

        final var visitsWithVisitors = bookingService.getBookingVisitsWithVisitor(VisitInformationFilter.builder().bookingId(-1L).build(), pageable);
        assertThat(visitsWithVisitors).containsOnly(
            VisitWithVisitors.builder()
                .visitDetail(
                    VisitDetails
                        .builder()
                        .cancellationReason(null)
                        .cancelReasonDescription(null)
                        .eventStatus("ATT")
                        .eventStatusDescription("Attended")
                        .eventOutcome("ATT")
                        .eventOutcomeDescription("Attended")
                        .startTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .endTime(LocalDateTime.parse("2019-10-10T15:00"))
                        .location("Visits")
                        .visitType("SOC")
                        .visitTypeDescription("Social")
                        .leadVisitor("John Smith")
                        .relationship("FRI")
                        .relationshipDescription("Friend")
                        .attended(true)
                        .searchType("FULL")
                        .searchTypeDescription("Full Search")
                        .build())
                .visitors(List.of(
                    Visitor
                        .builder()
                        .dateOfBirth(LocalDate.parse("1980-10-01"))
                        .firstName("John")
                        .lastName("Smith")
                        .leadVisitor(true)
                        .personId(-1L)
                        .relationship("Friend")
                        .attended(false)
                        .build(),
                    Visitor
                        .builder()
                        .dateOfBirth(LocalDate.parse("2010-10-01"))
                        .firstName("Jenny")
                        .lastName("Smith")
                        .leadVisitor(false)
                        .personId(-2L)
                        .relationship("Niece")
                        .attended(true)
                        .build()))
                .build());
    }

    @Test
    public void getBookingVisitsWithVisitor_filtered() {
        Pageable pageable = PageRequest.of(0, 20);
        var visits = List.of(VisitInformation
                .builder()
                .visitId(-1L)

                .visitorPersonId(-1L)
                .cancellationReason(null)
                .cancelReasonDescription(null)
                .eventStatus("ATT")
                .eventStatusDescription("Attended")
                .eventOutcome("ATT")
                .eventOutcomeDescription("Attended")
                .startTime(LocalDateTime.parse("2019-10-10T14:00"))
                .endTime(LocalDateTime.parse("2019-10-10T15:00"))
                .location("Visits")
                .visitType("SCON")
                .visitTypeDescription("Social")
                .leadVisitor("John Smith")
                .build(),
            VisitInformation
                .builder()
                .visitId(-2L)
                .bookingId(-1L)
                .visitorPersonId(-1L)
                .cancellationReason(null)
                .cancelReasonDescription(null)
                .eventStatus("ATT")
                .eventStatusDescription("Attended")
                .eventOutcome("ATT")
                .eventOutcomeDescription("Attended")
                .startTime(LocalDateTime.parse("2019-10-12T14:00"))
                .endTime(LocalDateTime.parse("2019-10-12T15:00"))
                .location("Visits")
                .visitType("SCON")
                .visitTypeDescription("Social")
                .leadVisitor("John Smith")
                .build());

        var page = new PageImpl<>(visits);
        when(offenderContactPersonsRepository.findAllByOffenderBooking_BookingIdAndPersonIdIn(-1L, List.of(-1L, -2L, -1L, -2L))).thenReturn(List.of(
            OffenderContactPerson.builder()
                .relationshipType(new RelationshipType("UN", "Uncle"))
                .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                .id(-1L)
                .personId(-1L)
                .build(),
            OffenderContactPerson.builder()
                .relationshipType(new RelationshipType("FRI", "Friend"))
                .modifyDateTime(LocalDateTime.parse("2019-10-11T14:00"))
                .id(-2L)
                .personId(-1L)
                .build(),
            OffenderContactPerson.builder()
                .relationshipType(new RelationshipType("NIE", "Niece"))
                .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                .id(-3L)
                .personId(-2L)
                .build()

        ));
        when(visitInformationRepository.findAll(VisitInformationFilter.builder()
            .bookingId(-1L)
            .fromDate(LocalDate.of(2019, 10, 10))
            .toDate(LocalDate.of(2019, 10, 12))
            .visitType("SCON")
            .build(), pageable))
            .thenReturn(page);


        when(visitVisitorRepository.findByVisitIdInAndOffenderBookingIsNullOrderByPerson_BirthDateDesc(List.of(-1L, -2L))).thenReturn(List.of(
            VisitVisitor.builder()
                .visitId(-1L)
                .person(Person.builder()
                    .id(-1L)
                    .birthDate(LocalDate.parse("1980-10-01"))
                    .firstName("John")
                    .lastName("Smith")
                    .build())
                .groupLeader(true)
                .eventOutcome(new EventOutcome("ABS", "Absent"))
                .build(),
            VisitVisitor.builder()
                .visitId(-1L)
                .person(Person.builder()
                    .id(-2L)
                    .birthDate(LocalDate.parse("2010-10-01"))
                    .firstName("Jenny")
                    .lastName("Smith")
                    .build())
                .groupLeader(false)
                .eventOutcome(new EventOutcome("ATT", "Attended"))
                .build(),
            VisitVisitor.builder()
                .visitId(-2L)
                .person(Person.builder()
                    .id(-1L)
                    .birthDate(LocalDate.parse("1980-10-01"))
                    .firstName("John")
                    .lastName("Smith")
                    .build())
                .groupLeader(true)
                .eventOutcome(new EventOutcome("ABS", "Absent"))
                .build(),
            VisitVisitor.builder()
                .visitId(-2L)
                .person(Person.builder()
                    .id(-2L)
                    .birthDate(LocalDate.parse("2010-10-01"))
                    .firstName("Jenny")
                    .lastName("Smith")
                    .build())
                .groupLeader(false)
                .eventOutcome(new EventOutcome("ATT", "Attended"))
                .build()

        ));

        final var visitsWithVisitors = bookingService.getBookingVisitsWithVisitor(
            VisitInformationFilter.builder()
                .bookingId(-1L)
                .fromDate(LocalDate.of(2019, 10, 10))
                .toDate(LocalDate.of(2019, 10, 12))
                .visitType("SCON")
                .build(),
            pageable);
        assertThat(visitsWithVisitors).containsOnly(
            VisitWithVisitors.builder()
                .visitDetail(
                    VisitDetails
                        .builder()
                        .cancellationReason(null)
                        .cancelReasonDescription(null)
                        .eventStatus("ATT")
                        .eventStatusDescription("Attended")
                        .eventOutcome("ATT")
                        .eventOutcomeDescription("Attended")
                        .startTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .endTime(LocalDateTime.parse("2019-10-10T15:00"))
                        .location("Visits")
                        .visitType("SCON")
                        .visitTypeDescription("Social")
                        .leadVisitor("John Smith")
                        .relationship("FRI")
                        .relationshipDescription("Friend")
                        .attended(true)
                        .build())
                .visitors(List.of(
                    Visitor
                        .builder()
                        .dateOfBirth(LocalDate.parse("1980-10-01"))
                        .firstName("John")
                        .lastName("Smith")
                        .leadVisitor(true)
                        .personId(-1L)
                        .relationship("Friend")
                        .attended(false)
                        .build(),
                    Visitor
                        .builder()
                        .dateOfBirth(LocalDate.parse("2010-10-01"))
                        .firstName("Jenny")
                        .lastName("Smith")
                        .leadVisitor(false)
                        .personId(-2L)
                        .relationship("Niece")
                        .attended(true)
                        .build()))
                .build(),
            VisitWithVisitors.builder()
                .visitDetail(
                    VisitDetails
                        .builder()
                        .cancellationReason(null)
                        .cancelReasonDescription(null)
                        .eventStatus("ATT")
                        .eventStatusDescription("Attended")
                        .eventOutcome("ATT")
                        .eventOutcomeDescription("Attended")
                        .startTime(LocalDateTime.parse("2019-10-12T14:00"))
                        .endTime(LocalDateTime.parse("2019-10-12T15:00"))
                        .location("Visits")
                        .visitType("SCON")
                        .visitTypeDescription("Social")
                        .leadVisitor("John Smith")
                        .relationship("FRI")
                        .relationshipDescription("Friend")
                        .attended(true)
                        .build())
                .visitors(List.of(
                    Visitor
                        .builder()
                        .dateOfBirth(LocalDate.parse("1980-10-01"))
                        .firstName("John")
                        .lastName("Smith")
                        .leadVisitor(true)
                        .personId(-1L)
                        .relationship("Friend")
                        .attended(false)
                        .build(),
                    Visitor
                        .builder()
                        .dateOfBirth(LocalDate.parse("2010-10-01"))
                        .firstName("Jenny")
                        .lastName("Smith")
                        .leadVisitor(false)
                        .personId(-2L)
                        .relationship("Niece")
                        .attended(true)
                        .build()))
                .build());
    }

    @Test
    public void getBookingVisitsWithVisitor_noVisitors() {
        Pageable pageable = PageRequest.of(0, 20);
        var visits = List.of(VisitInformation
            .builder()
            .visitId(-1L)
            .cancellationReason(null)
            .cancelReasonDescription(null)
            .startTime(LocalDateTime.parse("2019-10-10T14:00"))
            .endTime(LocalDateTime.parse("2019-10-10T15:00"))
            .location("Visits")
            .visitType("SOC")
            .visitTypeDescription("Social")
            .build());

        var page = new PageImpl<>(visits);
        when(visitInformationRepository.findAll(VisitInformationFilter.builder().bookingId(-1L).build(), pageable))
            .thenReturn(page);

        when(visitVisitorRepository.findByVisitIdInAndOffenderBookingIsNullOrderByPerson_BirthDateDesc(List.of(-1L)))
            .thenReturn(List.of());

        final var visitsWithVisitors = bookingService.getBookingVisitsWithVisitor(VisitInformationFilter.builder().bookingId(-1L).build(), pageable);
        assertThat(visitsWithVisitors).containsOnly(
            VisitWithVisitors.builder()
                .visitDetail(
                    VisitDetails
                        .builder()
                        .cancellationReason(null)
                        .cancelReasonDescription(null)
                        .eventStatus(null)
                        .eventStatusDescription(null)
                        .eventOutcome(null)
                        .eventOutcomeDescription(null)
                        .startTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .endTime(LocalDateTime.parse("2019-10-10T15:00"))
                        .location("Visits")
                        .visitType("SOC")
                        .visitTypeDescription("Social")
                        .leadVisitor(null)
                        .attended(false)
                        .build())
                .visitors(List.of())
                .build());
    }


    @Test
    void getOffenderCourtCases_active_only_mapped() {
        final var activeCourtCase = caseWithDefaults().id(-1L).caseSeq(-1).caseStatus(new CaseStatus("A", "Active")).build();
        final var inactiveCourtCase = caseWithDefaults().id(-2L).caseSeq(-2).caseStatus(new CaseStatus("I", "Inactive")).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
            .courtCases(List.of(activeCourtCase, inactiveCourtCase))
            .build()));

        final var activeOnlyCourtCases = bookingService.getOffenderCourtCases(-1L, true);

        assertThat(activeOnlyCourtCases).containsExactly(CourtCase.builder()
            .id(-1L)
            .caseSeq(-1)
            .beginDate(LocalDate.EPOCH)
            .agency(Agency.builder()
                .agencyId("agency_id")
                .active(true)
                .agencyType("CRT")
                .description("The Agency Description")
                .build())
            .caseType("Adult")
            .caseInfoPrefix("cip")
            .caseInfoNumber("cin")
            .caseStatus("Active")
            .courtHearings(Collections.emptyList())
            .build());
    }

    @Test
    void getOffenderCourtCases_all_mapped() {
        final var activeCourtCase = caseWithDefaults().id(-1L).caseSeq(-1).caseStatus(new CaseStatus("A", "Active")).build();
        final var inactiveCourtCase = caseWithDefaults().id(-2L).caseSeq(-2).caseStatus(new CaseStatus("I", "Inactive")).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
            .courtCases(List.of(activeCourtCase, inactiveCourtCase))
            .build()));

        final var allCourtCases = bookingService.getOffenderCourtCases(-1L, false);

        assertThat(allCourtCases).containsExactly(
            CourtCase.builder()
                .id(-1L)
                .caseSeq(-1)
                .beginDate(LocalDate.EPOCH)
                .agency(Agency.builder()
                    .agencyId("agency_id")
                    .active(true)
                    .agencyType("CRT")
                    .description("The Agency Description")
                    .build())
                .caseType("Adult")
                .caseInfoPrefix("cip")
                .caseInfoNumber("cin")
                .caseStatus("Active")
                .courtHearings(Collections.emptyList())
                .build(),
            CourtCase.builder()
                .id(-2L)
                .caseSeq(-2)
                .beginDate(LocalDate.EPOCH)
                .agency(Agency.builder()
                    .agencyId("agency_id")
                    .active(true)
                    .agencyType("CRT")
                    .description("The Agency Description")
                    .build())
                .caseType("Adult")
                .caseInfoPrefix("cip")
                .caseInfoNumber("cin")
                .caseStatus("Inactive")
                .courtHearings(Collections.emptyList())
                .build());
    }

    private OffenderCourtCase.OffenderCourtCaseBuilder caseWithDefaults() {
        return OffenderCourtCase.builder().beginDate(LocalDate.EPOCH)
            .agencyLocation(AgencyLocation.builder()
                .id("agency_id")
                .active(true)
                .type(AgencyLocationType.COURT_TYPE)
                .description("The agency description")
                .build())
            .legalCaseType(new LegalCaseType("A", "Adult"))
            .caseInfoPrefix("cip")
            .caseInfoNumber("cin");
    }

    @Test
    void getOffenderPropertyContainers() {
        final var activePropertyContainer = containerWithDefaults().containerId(-1L).active(true).build();
        final var inactivePropertyContainer = containerWithDefaults().containerId(-2L).active(false).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
            .propertyContainers(List.of(activePropertyContainer, inactivePropertyContainer))
            .build()));

        final var propertyContainers = bookingService.getOffenderPropertyContainers(-1L);

        assertThat(propertyContainers).containsExactly(uk.gov.justice.hmpps.prison.api.model.PropertyContainer.builder()
            .sealMark("TEST1")
            .location(Location.builder()
                .locationId(10L)
                .description(null)
                .subLocations(false)
                .build())
            .containerType("Bulk")
            .build());
    }

    @Test
    void getOffenderPropertyContainers_missingLocation() {
        final var activePropertyContainer = containerWithDefaults().containerId(-1L).internalLocation(null).active(true).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
            .propertyContainers(List.of(activePropertyContainer))
            .build()));

        final var propertyContainers = bookingService.getOffenderPropertyContainers(-1L);

        assertThat(propertyContainers).containsExactly(uk.gov.justice.hmpps.prison.api.model.PropertyContainer.builder()
            .sealMark("TEST1")
            .location(null)
            .containerType("Bulk")
            .build());
    }

    private OffenderPropertyContainer.OffenderPropertyContainerBuilder containerWithDefaults() {
        return OffenderPropertyContainer.builder()
            .sealMark("TEST1")
            .internalLocation(AgencyInternalLocation.builder()
                .active(true)
                .locationId(10L)
                .build())
            .containerType(new PropertyContainer("BULK", "Bulk"));
    }

    @Test
    void getSentenceAdjustments() {
        final var offenderSentenceAdjustments = List.of(
            SentenceAdjustment.builder()
                .id(-8L)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .sentenceAdjustCode("RSR")
                .active(true)
                .adjustDays(4)
                .build(),
            SentenceAdjustment.builder()
                .id(-9L)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .sentenceAdjustCode("RST")
                .active(false)
                .adjustDays(4)
                .build(),
            SentenceAdjustment.builder()
                .id(-10L)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .sentenceAdjustCode("RX")
                .active(true)
                .adjustDays(4)
                .build(),
            SentenceAdjustment.builder()
                .id(-11L)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .sentenceAdjustCode("S240A")
                .active(false)
                .adjustDays(4)
                .build(),
            SentenceAdjustment.builder()
                .id(-12L)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .sentenceAdjustCode("UR")
                .active(true)
                .adjustDays(4)
                .build(),
            SentenceAdjustment.builder()
                .id(-13L)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .sentenceAdjustCode("RX")
                .active(true)
                .adjustDays(4)
                .build()
        );

        final var offenderKeyDateAdjustments = List.of(
            KeyDateAdjustment
                .builder()
                .id(-8L)
                .sentenceAdjustCode("ADA")
                .active(true)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .adjustDays(4)
                .build(),
            KeyDateAdjustment
                .builder()
                .id(-9L)
                .sentenceAdjustCode("ADA")
                .active(false)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .adjustDays(9)
                .build(),
            KeyDateAdjustment
                .builder()
                .id(-10L)
                .sentenceAdjustCode("ADA")
                .active(true)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .adjustDays(13)
                .build(),
            KeyDateAdjustment
                .builder()
                .id(-11L)
                .sentenceAdjustCode("UAL")
                .active(false)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .adjustDays(1)
                .build(),
            KeyDateAdjustment
                .builder()
                .id(-12L)
                .sentenceAdjustCode("RADA")
                .active(true)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .adjustDays(2)
                .build(),
            KeyDateAdjustment
                .builder()
                .id(-13L)
                .sentenceAdjustCode("UAL")
                .active(true)
                .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
                .adjustDays(7)
                .build()
        );

        when(offenderBookingRepository.findById(-6L)).thenReturn(
            Optional.of(OffenderBooking.builder()
                .keyDateAdjustments(offenderKeyDateAdjustments)
                .sentenceAdjustments(offenderSentenceAdjustments)
                .build()));

        final SentenceAdjustmentDetail sentenceAdjustmentDetail = bookingService.getBookingSentenceAdjustments(-6L);

        assertThat(sentenceAdjustmentDetail).isEqualTo(
            SentenceAdjustmentDetail.builder()
                .additionalDaysAwarded(17)
                .unlawfullyAtLarge(7)
                .restoredAdditionalDaysAwarded(2)
                .recallSentenceRemand(4)
                .remand(8)
                .unusedRemand(4)
                .build());
    }


    @Test
    void getOffenderSentenceDetail_most_recent_active_booking() {
        final var offender =
            Offender.builder().bookings(
                    List.of(
                        OffenderBooking.builder()
                            .bookingSequence(2)
                            .active(false)
                            .location(AgencyLocation.builder()
                                .description("Agency Description 2 An Inactive Booking")
                                .build())
                            .build(),
                        OffenderBooking.builder()
                            .bookingSequence(1)
                            .active(true)
                            .location(AgencyLocation.builder()
                                .description("Agency Description 1 An Active Booking")
                                .build())
                            .build()))
                .build();

        when(offenderRepository.findOffenderWithLatestBookingByNomsId("NomsId")).thenReturn(Optional.of(offender));
        Optional<OffenderSentenceDetail> offenderSentenceDetail = bookingService.getOffenderSentenceDetail("NomsId");

        assertThat(offenderSentenceDetail)
            .isNotEmpty()
            .map(OffenderSentenceDetail::getMostRecentActiveBooking)
            .hasValue(true);

        assertThat(offenderSentenceDetail)
            .map(OffenderSentenceDetail::getAgencyLocationDesc)
            .hasValue("Agency Description 1 An Active Booking");
    }

    @Test
    void getOffenderSentencesSummary_most_recent_active_booking() {
        final var OffenderSentenceDetailDtos
            = List.of(
            OffenderSentenceDetailDto.builder()
                .bookingId(1L)
                .mostRecentActiveBooking(true)
                .build(),
            OffenderSentenceDetailDto.builder()
                .bookingId(2L)
                .mostRecentActiveBooking(false)
                .build());

        when(bookingRepository.getOffenderSentenceSummary(any(), any(), anyBoolean(), anyBoolean())).thenReturn(OffenderSentenceDetailDtos);
        List<OffenderSentenceDetail> offenderSentenceDetails = bookingService.getOffenderSentencesSummary(null, List.of("NomsId"));

        assertThat(offenderSentenceDetails).hasSize(2);
        assertThat(offenderSentenceDetails.get(0)).extracting(OffenderSentenceDetail::getBookingId).isEqualTo(1L);
        assertThat(offenderSentenceDetails.get(0)).extracting(OffenderSentenceDetail::getMostRecentActiveBooking).isEqualTo(true);
        assertThat(offenderSentenceDetails.get(1)).extracting(OffenderSentenceDetail::getBookingId).isEqualTo(2L);
        assertThat(offenderSentenceDetails.get(1)).extracting(OffenderSentenceDetail::getMostRecentActiveBooking).isEqualTo(false);
    }

    @Test
    void getOffenderCourtCases_errors_for_unknown_booking() {
        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getOffenderCourtCases(-1L, true))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Offender booking with id -1 not found.");
    }

    @Test
    void getOffenderPropertyContainers_errors_for_unknown_booking() {
        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getOffenderPropertyContainers(-1L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Offender booking with id -1 not found.");
    }

    private ScheduledEvent createEvent(final String type, final String time) {
        return ScheduledEvent.builder().bookingId(-1L)
            .startTime(Optional.ofNullable(time).map(t -> "2019-01-02T" + t).map(LocalDateTime::parse).orElse(null))
            .eventStatus("SCH")
            .eventType(type + time).build();
    }

    @Test
    public void getOffenderSentenceSummaries_forOveriddenRole() {
        when(authenticationFacade.isOverrideRole(any(String[].class))).thenReturn(true);
        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(any())).thenReturn(List.of());
        assertThatThrownBy(() -> bookingService.getOffenderSentencesSummary(null, List.of()))
            .isInstanceOf(HttpClientErrorException.class).hasMessage("400 Request must be restricted to either a caseload, agency or list of offenders");
    }

    @Test
    void getSentenceAndOffenceDetails_withMinimalData() {
        final var bookingId = -1L;
        when(offenderSentenceRepository.findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(bookingId, "%AGG%", "LICENCE"))
            .thenReturn(
                List.of(OffenderSentence.builder()
                    .id(new OffenderSentence.PK(-99L, 1))
                    .calculationType(SentenceCalcType.builder().build()
                    ).build()
                )
            );

        final var sentencesAndOffences = bookingService.getSentenceAndOffenceDetails(bookingId);

        assertThat(sentencesAndOffences).containsExactly(
            OffenderSentenceAndOffences.builder()
                .bookingId(-99L)
                .sentenceSequence(1)
                .build()
        );
    }

    @Test
    void getSentenceAndOffenceDetails_withFullData() {
        final var bookingId = -1L;

        final var terms = List.of(
            SentenceTerm.builder()
                .id(new SentenceTerm.PK(1L, 1, 1))
                .years(2)
                .sentenceTermCode("IMP")
                .build(),
            SentenceTerm.builder()
                .id(new SentenceTerm.PK(1L, 1, 2))
                .years(1)
                .sentenceTermCode("LI")
                .build());

        when(offenderSentenceRepository.findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(bookingId, "%AGG%", "LICENCE"))
            .thenReturn(
                List.of(OffenderSentence.builder()
                    .id(new OffenderSentence.PK(-98L, 2))
                    .lineSequence(5L)
                    .consecutiveToSentenceSequence(1)
                    .status("A")
                    .calculationType(
                        SentenceCalcType.builder()
                            .category("CAT")
                            .calculationType("CALC")
                            .description("Calc description")
                            .build()
                    )
                    .courtOrder(
                        CourtOrder.builder()
                            .courtDate(LocalDate.of(2021, 1, 1))
                            .build()
                    )
                    .terms(terms)
                    .offenderSentenceCharges(List.of(
                        OffenderSentenceCharge.builder()
                            .offenderCharge(OffenderCharge.builder()
                                .dateOfOffence(LocalDate.of(2021, 1, 2))
                                .endDate(LocalDate.of(2021, 1, 25))
                                .offence(Offence.builder()
                                    .offenceIndicators(List.of(
                                        OffenceIndicator.builder().indicatorCode("INDICATOR").build()
                                    ))
                                    .code("STA1234")
                                    .statute(
                                        Statute.builder().code("STA").build()
                                    )
                                    .build())
                                .build()
                            )
                            .build()
                    ))
                    .courtCase(
                        OffenderCourtCase.builder()
                            .caseSeq(10)
                            .caseInfoNumber("XYZ789")
                            .courtEvents(
                                List.of(CourtEvent.builder()
                                    .eventDate(LocalDate.of(2021, 1, 1))
                                    .courtLocation(
                                        AgencyLocation.builder()
                                            .description("A court")
                                            .build()
                                    )
                                    .build())
                            )
                            .build()
                    )
                    .build()
                )
            );

        final var sentencesAndOffences = bookingService.getSentenceAndOffenceDetails(bookingId);

        assertThat(sentencesAndOffences).containsExactly(
            OffenderSentenceAndOffences.builder()
                .bookingId(-98L)
                .sentenceSequence(2)
                .lineSequence(5L)
                .caseSequence(10)
                .caseReference("XYZ789")
                .courtDescription("A court")
                .consecutiveToSequence(1)
                .sentenceStatus("A")
                .sentenceCategory("CAT")
                .sentenceCalculationType("CALC")
                .sentenceTypeDescription("Calc description")
                .sentenceDate(LocalDate.of(2021, 1, 1))
                .terms(List.of(
                    OffenderSentenceTerm.builder()
                        .days(0)
                        .weeks(0)
                        .months(0)
                        .years(2)
                        .code("IMP")
                        .build(),
                    OffenderSentenceTerm.builder()
                        .days(0)
                        .weeks(0)
                        .months(0)
                        .years(1)
                        .code("LI")
                        .build()
                ))
                .offences(List.of(
                    OffenderOffence.builder()
                        .offenceStartDate(LocalDate.of(2021, 1, 2))
                        .offenceEndDate(LocalDate.of(2021, 1, 25))
                        .offenceStatute("STA")
                        .offenceCode("STA1234")
                        .indicators(List.of("INDICATOR"))
                        .build()
                ))
                .build()
        );
    }

    @Test
    void getOffenderFinePayments() {
        final var bookingId = -1L;
        when(offenderFinePaymentRepository.findByOffenderBooking_BookingId(bookingId))
            .thenReturn(
                List.of(OffenderFinePayment.builder()
                    .offenderBooking(OffenderBooking.builder().bookingId(-99L).build())
                    .sequence(5)
                    .paymentDate(LocalDate.of(2022, 1, 1))
                    .paymentAmount(new BigDecimal("9.99"))
                    .build()
                )
            );

        final var sentencesAndOffences = bookingService.getOffenderFinePayments(bookingId);

        assertThat(sentencesAndOffences).containsExactly(
            OffenderFinePaymentDto.builder()
                .bookingId(-99L)
                .sequence(5)
                .paymentDate(LocalDate.of(2022, 1, 1))
                .paymentAmount(new BigDecimal("9.99"))
                .build()
        );
    }

    @Test
    public void getOffenderCourtEventOutcomes() {
        final var bookingIds = Set.of(1L, 2L);

        final var courtEvent1 = CourtEvent.builder()
            .id(54L)
            .offenderBooking(new OffenderBooking().withBookingId(1L))
            .outcomeReasonCode(new OffenceResult().withCode("4016"))
            .build();
        final var courtEvent2 = CourtEvent.builder()
            .id(93L)
            .offenderBooking(new OffenderBooking().withBookingId(2L))
            .outcomeReasonCode(new OffenceResult().withCode("5011"))
            .build();

        final var courtEvents =  List.of(courtEvent1, courtEvent2);
        when(courtEventRepository.findByOffenderBooking_BookingIdInAndOffenderCourtCase_CaseStatus_Code(bookingIds, "A")).thenReturn(courtEvents);
        final var courtEventOutcomes = bookingService.getOffenderCourtEventOutcomes(bookingIds);

        assertThat(courtEventOutcomes.size()).isEqualTo(2);
        assertThat(courtEventOutcomes.get(0).getOutcomeReasonCode()).isEqualTo("4016");
        assertThat(courtEventOutcomes.get(0).getBookingId()).isEqualTo(1L);
        assertThat(courtEventOutcomes.get(1).getOutcomeReasonCode()).isEqualTo("5011");
        assertThat(courtEventOutcomes.get(1).getBookingId()).isEqualTo(2L);
    }

    @Nested
    class UpdateLivingUnit {

        final Long SOME_BOOKING_ID = 1L;
        final Long BAD_BOOKING_ID = 2L;
        final Long OLD_LIVING_UNIT_ID = 11L;
        final Long NEW_LIVING_UNIT_ID = 12L;
        final String NEW_LIVING_UNIT_DESC = "Z-1";
        final String SOME_AGENCY_ID = "MDI";
        final String LOCATION_CODE = "RECP";
        final String DIFFERENT_AGENCY_ID = "NOT_MDI";

        @Test
        void bookingNotFound_throws() {
            when(offenderBookingRepository.findById(BAD_BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateLivingUnit(BAD_BOOKING_ID, aCellSwapLocation()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(valueOf(BAD_BOOKING_ID));
        }

        @Test
        void livingUnitNotCell_throws() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));

            assertThatThrownBy(() -> bookingService.updateLivingUnit(SOME_BOOKING_ID, aLocation(NEW_LIVING_UNIT_ID, SOME_AGENCY_ID, "WING")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NEW_LIVING_UNIT_DESC)
                .hasMessageContaining("WING");
        }

        @Test
        void differentAgency_throws() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));

            assertThatThrownBy(() -> bookingService.updateLivingUnit(SOME_BOOKING_ID, aLocation(NEW_LIVING_UNIT_ID, DIFFERENT_AGENCY_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(SOME_AGENCY_ID)
                .hasMessageContaining(DIFFERENT_AGENCY_ID);
        }

        @Test
        void ok_updatesRepo() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));

            bookingService.updateLivingUnit(SOME_BOOKING_ID, aLocation(NEW_LIVING_UNIT_ID, SOME_AGENCY_ID));

            ArgumentCaptor<OffenderBooking> updatedOffenderBooking = ArgumentCaptor.forClass(OffenderBooking.class);
            verify(offenderBookingRepository).save(updatedOffenderBooking.capture());
            assertThat(updatedOffenderBooking.getValue().getAssignedLivingUnit().getLocationId()).isEqualTo(NEW_LIVING_UNIT_ID);
        }

        @Test
        void ok_updatesRepoForReception() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));

            bookingService.updateLivingUnit(SOME_BOOKING_ID, receptionLocation(NEW_LIVING_UNIT_ID, LOCATION_CODE));

            ArgumentCaptor<OffenderBooking> updatedOffenderBooking = ArgumentCaptor.forClass(OffenderBooking.class);
            verify(offenderBookingRepository).save(updatedOffenderBooking.capture());
            assertThat(updatedOffenderBooking.getValue().getAssignedLivingUnit().getLocationId()).isEqualTo(NEW_LIVING_UNIT_ID);
        }

        @Test
        void cellSwap() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));

            final var cellSwapLocation = aCellSwapLocation();

            bookingService.updateLivingUnit(SOME_BOOKING_ID, cellSwapLocation);

            ArgumentCaptor<OffenderBooking> updatedOffenderBooking = ArgumentCaptor.forClass(OffenderBooking.class);
            verify(offenderBookingRepository).save(updatedOffenderBooking.capture());

            assertThat(updatedOffenderBooking.getValue().getAssignedLivingUnit().getLocationId()).isEqualTo(cellSwapLocation.getLocationId());
        }

        private Optional<OffenderBooking> anOffenderBooking(final Long bookingId, final Long livingUnitId) {
            final var agencyLocation = AgencyLocation.builder().id("MDI").build();
            final var livingUnit = AgencyInternalLocation.builder().locationId(livingUnitId).build();
            final var offender = Offender.builder().nomsId("any noms id").build();
            return Optional.of(
                OffenderBooking.builder()
                    .bookingId(bookingId)
                    .assignedLivingUnit(livingUnit)
                    .location(agencyLocation)
                    .offender(offender)
                    .build());
        }

        private AgencyInternalLocation aCellSwapLocation() {
            return AgencyInternalLocation.builder()
                .locationId(-99L)
                .locationCode("CSWAP")
                .certifiedFlag(false)
                .active(true)
                .agencyId("MDI")
                .description("CSWAP-MDI")
                .build();
        }

        private AgencyInternalLocation aLocation(final Long locationId, final String agencyId) {
            return aLocation(locationId, agencyId, "CELL");
        }

        private AgencyInternalLocation aLocation(final Long locationId, final String agencyId, final String locationType) {
            return AgencyInternalLocation.builder()
                .locationId(locationId)
                .description("Z-1")
                .agencyId(agencyId)
                .locationType(locationType)
                .build();
        }
        private AgencyInternalLocation receptionLocation(final Long locationId, final String locationCode) {
            return
                AgencyInternalLocation.builder()
                    .locationId(locationId)
                    .operationalCapacity(null)
                    .currentOccupancy(1)
                    .locationType("AREA")
                    .agencyId("MDI")
                    .locationCode(locationCode)
                    .userDescription(null)
                    .certifiedFlag(false)
                    .active(true)
                    .capacity(100)
                    .description("MDI-RECP")
                    .livingUnit(null)
                    .build();
        }

    }

    @Nested
    class GetBookingVisitsSummary {
        @Test
        void hasVisits() {
            when(visitInformationRepository.countByBookingId(anyLong())).thenReturn(5L);
            final var startTime = LocalDateTime.parse("2020-02-01T10:20:30");
            when(bookingRepository.getBookingVisitNext(anyLong(), any())).thenReturn(Optional.of(
                VisitDetails.builder().startTime(startTime).build()
            ));
            final var summary = bookingService.getBookingVisitsSummary(-1L);
            assertThat(summary.getStartDateTime()).isEqualTo(startTime);
            assertThat(summary.getHasVisits()).isTrue();
        }

        @Test
        void noVisits() {
            when(visitInformationRepository.countByBookingId(anyLong())).thenReturn(0L);
            when(bookingRepository.getBookingVisitNext(anyLong(), any())).thenReturn(Optional.empty());
            final var summary = bookingService.getBookingVisitsSummary(-1L);
            assertThat(summary.getStartDateTime()).isNull();
            assertThat(summary.getHasVisits()).isFalse();
        }
    }

    @Test
    public void getActiveOffencesForBookings() {
        final var bookingIds = Set.of(2L, 4L, 9L);
        final var charges = List.of(new OffenderCharge());

        when(offenderChargeRepository.findByOffenderBooking_BookingIdInAndChargeStatusAndOffenderCourtCase_CaseStatus_Code(bookingIds, "A", "A")).thenReturn(charges);
        List<OffenceHistoryDetail> offenceHistoryDetails = bookingService.getActiveOffencesForBookings(bookingIds);

        verify(offenderChargeRepository).findByOffenderBooking_BookingIdInAndChargeStatusAndOffenderCourtCase_CaseStatus_Code(bookingIds, "A", "A");
        assertThat(offenceHistoryDetails).isNotNull();
        assertThat(offenceHistoryDetails).hasSize(charges.size());
    }

    @Test
    public void givenCalculationsThenReturnAll() {
        var sentenceCalculationSummaries = offenderSentenceCalculationSummaries();
        when(bookingRepository.getOffenderSentenceCalculationsForPrisoner(anyString())).thenReturn(sentenceCalculationSummaries);

        final var results = bookingService.getOffenderSentenceCalculationsForPrisoner("ABZ123A");

        verify(bookingRepository).getOffenderSentenceCalculationsForPrisoner(eq("ABZ123A"));
        assertThat(results).hasSize(sentenceCalculationSummaries.size());
    }

    private List<SentenceCalculationSummary> offenderSentenceCalculationSummaries() {
        return List.of(
            sentenceCalculationSummary(1L),
            sentenceCalculationSummary(2L),
            sentenceCalculationSummary(3L),
            sentenceCalculationSummary(4L),
            sentenceCalculationSummary(5L));
    }

    private SentenceCalculationSummary sentenceCalculationSummary(final Long bookingId) {
        return new SentenceCalculationSummary(bookingId, "ABC", "first name", "last name", "SYI", "Shrewsbury", 1, LocalDateTime.now(), 1L, "comment", "Adjust Sentence", "user");
    }
}
