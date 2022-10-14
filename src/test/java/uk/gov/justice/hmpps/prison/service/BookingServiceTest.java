package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFinePayment;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.BookingActivity;
import uk.gov.justice.hmpps.prison.api.model.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.IepLevelAndComment;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecord;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords;
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderOffence;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetailDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerm;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeDetail;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentDetail;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.VisitWithVisitors;
import uk.gov.justice.hmpps.prison.api.model.Visitor;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AvailablePrisonIepLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AvailablePrisonIepLevel.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtOrder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.IepLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceIndicator;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderContactPerson;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderHealthProblem;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIepLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalcType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitorInformation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AvailablePrisonIepLevelRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderContactPersonsRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderFinePaymentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRestrictionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitorRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderBookingTransformer;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link BookingService}.
 */
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private OffenderBookingRepository offenderBookingRepository;
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private VisitInformationRepository visitInformationRepository;
    @Mock
    private VisitorRepository visitorRepository;
    @Mock
    private AgencyService agencyService;
    @Mock
    private AgencyInternalLocationRepository agencyInternalLocationRepository;

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
    private AvailablePrisonIepLevelRepository availablePrisonIepLevelRepository;
    @Mock
    private OffenderSentenceRepository offenderSentenceRepository;

    @Mock
    private OffenderFinePaymentRepository offenderFinePaymentRepository;
    @Mock
    private CaseloadToAgencyMappingService caseloadToAgencyMappingService;
    @Mock
    private CaseLoadService caseLoadService;

    private BookingService bookingService;

    @BeforeEach
    public void init() {
        bookingService = new BookingService(
                bookingRepository,
                offenderBookingRepository,
                offenderRepository,
                visitorRepository,
                visitInformationRepository,
                null,
                agencyService,
                caseLoadService,
                caseloadToAgencyMappingService,
                agencyInternalLocationRepository,
                offenderContactPersonsRepository,
                staffUserAccountRepository,
                offenderBookingTransformer,
                offenderTransformer,
                authenticationFacade,
                offenderSentenceRepository,
                offenderFinePaymentRepository,
                availablePrisonIepLevelRepository,
                offenderRestrictionRepository,"1",
                10);
    }

    @Test
    public void testVerifyCanAccessLatestBooking() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true);

        bookingService.getOffenderIdentifiers("off-1");
    }

    @Test
    public void testVerifyCannotAccessLatestBooking() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false);

        assertThatThrownBy(() ->
                bookingService.getOffenderIdentifiers("off-1"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true);


        bookingService.getOffenderIdentifiers("off-1");
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo_systemUser() {
        when(authenticationFacade.isOverrideRole(any())).thenReturn(true);

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", -1L, 1)));

        bookingService.getOffenderIdentifiers("off-1", "SYSTEM_USER", "GLOBAL_SEARCH");

        verify(authenticationFacade).isOverrideRole(
                "SYSTEM_USER", "GLOBAL_SEARCH"
        );
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo_not() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getLatestBookingIdentifierForOffender("off-1")).thenReturn(Optional.of(new OffenderBookingIdSeq("off-1", bookingId, 1)));
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false);

        assertThatThrownBy(() ->
                bookingService.getOffenderIdentifiers("off-1"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void givenValidBookingIdIepLevelAndComment_whenIepLevelAdded() {
        final var bookingId = 1L;
        final var iepLevelAndComment = IepLevelAndComment.builder().iepLevel("STD").comment("Comment").reviewerUserName("FRED").build();

        final var leedsPrison = AgencyLocation.builder().id("LEI").build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(OffenderBooking.builder().bookingId(bookingId).location(leedsPrison).build()));
        when(availablePrisonIepLevelRepository.findById(eq(new PK(iepLevelAndComment.getIepLevel(), leedsPrison)))).thenReturn(Optional.of(AvailablePrisonIepLevel.builder().iepLevel(new IepLevel("STD", "Standard")).agencyLocation(leedsPrison).build()));
        when(staffUserAccountRepository.findById("FRED")).thenReturn(Optional.of(StaffUserAccount.builder().username("FRED").build()));
        bookingService.addIepLevel(bookingId, iepLevelAndComment);

        verify(availablePrisonIepLevelRepository).findById(eq(new PK(iepLevelAndComment.getIepLevel(), leedsPrison)));
    }

    @Test
    public void givenInvalidIepLevel_whenIepLevelAdded() {
        final var bookingId = 1L;
        final var iepLevelAndComment = IepLevelAndComment.builder().iepLevel("STD").comment("Comment").reviewerUserName("FRED").build();

        final var leedsPrison = AgencyLocation.builder().id("LEI").build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(OffenderBooking.builder().bookingId(bookingId).location(leedsPrison).build()));
        when(availablePrisonIepLevelRepository.findById(eq(new PK(iepLevelAndComment.getIepLevel(), leedsPrison)))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.addIepLevel(bookingId, iepLevelAndComment))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("IEP Level 'STD' is not active for this booking's agency: Booking Id 1.");
    }

    @Test
    public void testThatUpdateAttendanceIsCalledForEachBooking() {
        final var bookingIds = Set.of(1L, 2L, 3L);
        final var activityId = 2L;

        when(bookingRepository.getLatestBookingByBookingId(anyLong()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(1L).build()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(2L).build()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(3L).build()));

        when(bookingRepository.getAttendanceEventDate(anyLong())).thenReturn(LocalDate.now());
        when(bookingRepository.getPayableAttendanceOutcome(anyString(), anyString()))
                .thenReturn(PayableAttendanceOutcomeDto
                        .builder()
                        .paid(true)
                        .build());

        final var updateAttendance = UpdateAttendance
                .builder()
                .eventOutcome("ATT")
                .performance("STANDARD")
                .build();

        final var bookingActivities = bookingIds.stream()
                .map(bookingId -> BookingActivity.builder().bookingId(bookingId).activityId(activityId).build())
                .collect(Collectors.toSet());

        bookingService.updateAttendanceForMultipleBookingIds(bookingActivities, updateAttendance);

        final var expectedOutcome = UpdateAttendance.builder().performance("STANDARD").eventOutcome("ATT").build();

        bookingIds.forEach(bookingId -> verify(bookingRepository).updateAttendance(bookingId, activityId, expectedOutcome, true, false));
    }

    @Test
    public void getBookingIEPSummary_singleBooking_withDetail_noPrivileges() {
        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(-1L, true))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_withDetail_noPrivileges() {
        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(List.of(-1L, -2L), true))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_singleBooking_noPrivileges() {
        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(-1L, false))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_noPrivileges() {
        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(List.of(-1L, -2L), false))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_returnOffenderCurrentIepLevel() {
        when(offenderBookingRepository.findById(-1L))
            .thenReturn(Optional.of(OffenderBooking.builder()
                    .iepLevels(List.of(
                        OffenderIepLevel.builder()
                            .iepLevel(new IepLevel("", "Standard"))
                            .iepDate(LocalDate.now().minusDays(1))
                            .build()))
                .build()));
        final var result = bookingService.getBookingIEPSummary(-1L, false);
        assertThat(result.getIepLevel()).isEqualTo("Standard");
        assertThat(result.getDaysSinceReview()).isEqualTo(1L);
    }

    @Test
    public void getBookingIEPSummary_offenderHasNoIepSummary() {
        final var defaultIepLevel = "Standard";
        when(offenderBookingRepository.findById(-1L))
            .thenReturn(Optional.of(OffenderBooking.builder()
                .location(AgencyLocation.builder().id("MDI").build())
                .build()));
        when(availablePrisonIepLevelRepository.findByAgencyLocation_IdAndDefaultIep("MDI", true))
            .thenReturn(List.of(
                AvailablePrisonIepLevel.builder().iepLevel(new IepLevel("", defaultIepLevel)).build()));
        final var result = bookingService.getBookingIEPSummary(-1L, false);
        assertThat(result.getIepLevel()).isEqualTo(defaultIepLevel);
        assertThat(result.getDaysSinceReview()).isEqualTo(0L);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_globalSearchUser() {
        when(authenticationFacade.isOverrideRole(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        when(bookingRepository.getBookingIEPDetailsByBookingIds(anyList())).thenReturn(Map.of(-5L, List.of(PrivilegeDetail.builder().iepDate(LocalDate.now()).build())));
        when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OffenderBooking.builder().location(AgencyLocation.builder().id("MDI").build()).build()));
        assertThat(bookingService.getBookingIEPSummary(List.of(-1L, -2L), false)).containsKeys(-5L);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_withDetail_systemUser() {
        when(authenticationFacade.isOverrideRole()).thenReturn(true);
        when(bookingRepository.getBookingIEPDetailsByBookingIds(anyList())).thenReturn(Map.of(-5L, List.of(PrivilegeDetail.builder().iepDate(LocalDate.now()).build())));
        when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OffenderBooking.builder().location(AgencyLocation.builder().id("MDI").build()).build()));
        assertThat(bookingService.getBookingIEPSummary(List.of(-1L, -2L), true)).containsKeys(-5L);
    }

    @Test
    public void getBookingIEPSummary_oneOffenderHasNoIepSummary() {
        when(authenticationFacade.isOverrideRole()).thenReturn(true);
        when(bookingRepository.getBookingIEPDetailsByBookingIds(anyList())).thenReturn(Map.of(-5L, List.of(PrivilegeDetail.builder().iepDate(LocalDate.now()).build())));

        final var defaultIepLevel = "Standard";
        when(offenderBookingRepository.findById(anyLong()))
            .thenReturn(Optional.of(OffenderBooking.builder()
                .location(AgencyLocation.builder().id("MDI").build())
                .build()));
        when(availablePrisonIepLevelRepository.findByAgencyLocation_IdAndDefaultIep("MDI", true))
            .thenReturn(List.of(
                AvailablePrisonIepLevel.builder().iepLevel(new IepLevel("", defaultIepLevel)).build()));

        final var result = bookingService.getBookingIEPSummary(List.of(-1L, -2L), true);
        assertThat(result.get(-1L).getIepLevel()).isEqualTo(defaultIepLevel);
        assertThat(result.get(-1L).getDaysSinceReview()).isEqualTo(0L);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_withDetail_onlyAccessToOneBooking() {
        final Long bookingId = -1L;

        final var agencyIds = Set.of("LEI");
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(List.of(-1L, -2L), true)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_withDetail_accessToBothBookings() {
        final var agencyIds = Set.of("LEI");
        when(agencyService.getAgencyIds()).thenReturn(agencyIds).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(anyLong(), any())).thenReturn(true).thenReturn(true);
        when(bookingRepository.getBookingIEPDetailsByBookingIds(anyList())).thenReturn(Map.of(-5L, List.of(PrivilegeDetail.builder().iepDate(LocalDate.now()).build())));
        when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OffenderBooking.builder().location(AgencyLocation.builder().id("MDI").build()).build()));

        assertThat(bookingService.getBookingIEPSummary(List.of(-1L, -2L), true)).containsKeys(-5L);
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
        when(offenderContactPersonsRepository.findAllByPersonIdAndOffenderBooking_BookingId(-1L, -1L)).thenReturn(List.of(
                OffenderContactPerson.builder()
                        .relationshipType(new RelationshipType("UN", "Uncle"))
                        .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .createDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .id(-1L)
                        .build(),
                OffenderContactPerson.builder()
                        .relationshipType(new RelationshipType("FRI", "Friend"))
                        .modifyDateTime(null)
                        .createDateTime(LocalDateTime.parse("2019-10-11T14:00"))
                        .id(-2L)
                        .build()
        ));
        when(offenderContactPersonsRepository.findAllByPersonIdAndOffenderBooking_BookingId(-2L, -1L)).thenReturn(List.of(
                OffenderContactPerson.builder()
                        .relationshipType(new RelationshipType("NIE", "Niece"))
                        .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .id(-3L)
                        .build()
        ));
        when(visitInformationRepository.findAll(VisitInformationFilter.builder().bookingId(-1L).build(), pageable))
                .thenReturn(page);

        when(visitorRepository.findAllByVisitId(anyLong())).thenReturn(List.of(
                VisitorInformation
                        .builder()
                        .birthdate(LocalDate.parse("1980-10-01"))
                        .firstName("John")
                        .lastName("Smith")
                        .leadVisitor("Y")
                        .personId(-1L)
                        .visitId(-1L)
                        .build(),
                VisitorInformation
                        .builder()
                        .birthdate(LocalDate.parse("2010-10-01"))
                        .firstName("Jenny")
                        .lastName("Smith")
                        .leadVisitor("N")
                        .personId(-2L)
                        .visitId(-1L)
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
                                        .build(),
                                Visitor
                                        .builder()
                                        .dateOfBirth(LocalDate.parse("2010-10-01"))
                                        .firstName("Jenny")
                                        .lastName("Smith")
                                        .leadVisitor(false)
                                        .personId(-2L)
                                        .relationship("Niece")
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
                        .visitId(-1L)
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
        when(offenderContactPersonsRepository.findAllByPersonIdAndOffenderBooking_BookingId(-1L, -1L)).thenReturn(List.of(
                OffenderContactPerson.builder()
                        .relationshipType(new RelationshipType("UN", "Uncle"))
                        .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .id(-1L)
                        .build(),
                OffenderContactPerson.builder()
                        .relationshipType(new RelationshipType("FRI", "Friend"))
                        .modifyDateTime(LocalDateTime.parse("2019-10-11T14:00"))
                        .id(-2L)
                        .build()
        ));
        when(offenderContactPersonsRepository.findAllByPersonIdAndOffenderBooking_BookingId(-2L, -1L)).thenReturn(List.of(
                OffenderContactPerson.builder()
                        .relationshipType(new RelationshipType("NIE", "Niece"))
                        .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .id(-3L)
                        .build()
        ));
        when(visitInformationRepository.findAll(VisitInformationFilter.builder()
                .bookingId(-1L)
                .fromDate(LocalDate.of(2019, 10, 10))
                .toDate(LocalDate.of(2019, 10, 12))
                .visitType("SCON")
                .build(), pageable))
                .thenReturn(page);

        when(visitorRepository.findAllByVisitId(anyLong())).thenReturn(List.of(
                VisitorInformation
                        .builder()
                        .birthdate(LocalDate.parse("1980-10-01"))
                        .firstName("John")
                        .lastName("Smith")
                        .leadVisitor("Y")
                        .personId(-1L)
                        .visitId(-1L)
                        .build(),
                VisitorInformation
                        .builder()
                        .birthdate(LocalDate.parse("2010-10-01"))
                        .firstName("Jenny")
                        .lastName("Smith")
                        .leadVisitor("N")
                        .personId(-2L)
                        .visitId(-1L)
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
                                        .build(),
                                Visitor
                                        .builder()
                                        .dateOfBirth(LocalDate.parse("2010-10-01"))
                                        .firstName("Jenny")
                                        .lastName("Smith")
                                        .leadVisitor(false)
                                        .personId(-2L)
                                        .relationship("Niece")
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
                                        .build(),
                                Visitor
                                        .builder()
                                        .dateOfBirth(LocalDate.parse("2010-10-01"))
                                        .firstName("Jenny")
                                        .lastName("Smith")
                                        .leadVisitor(false)
                                        .personId(-2L)
                                        .relationship("Niece")
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

        when(visitorRepository.findAllByVisitId(anyLong())).thenReturn(List.of());

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
    public void getBookingVisitsWithVisitor_visitorNoPerson() {
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
                .build());

        var page = new PageImpl<>(visits);
        when(offenderContactPersonsRepository.findAllByPersonIdAndOffenderBooking_BookingId(-1L, -1L)).thenReturn(List.of(
                OffenderContactPerson.builder()
                        .relationshipType(new RelationshipType("UN", "Uncle"))
                        .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
                        .id(-1L)
                        .build(),
                OffenderContactPerson.builder()
                        .relationshipType(new RelationshipType("FRI", "Friend"))
                        .modifyDateTime(LocalDateTime.parse("2019-10-11T14:00"))
                        .id(-2L)
                        .build()
        ));
        when(visitInformationRepository.findAll(VisitInformationFilter.builder().bookingId(-1L).build(), pageable))
                .thenReturn(page);

        when(visitorRepository.findAllByVisitId(anyLong())).thenReturn(List.of(
                VisitorInformation
                        .builder()
                        .birthdate(LocalDate.parse("1980-10-01"))
                        .firstName("John")
                        .lastName("Smith")
                        .leadVisitor("Y")
                        .personId(-1L)
                        .visitId(-1L)
                        .build(),
                VisitorInformation
                        .builder()
                        .birthdate(LocalDate.parse("2010-10-01"))
                        .firstName("Jenny")
                        .lastName("Smith")
                        .leadVisitor("N")
                        .visitId(-1L)
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
                                        .build()))
                        .build());
    }


    @Test
    void getOffenderCourtCases_active_only_mapped() {
        final var activeCourtCase = caseWithDefaults().id(-1L).caseSeq(-1L).caseStatus(new CaseStatus("A", "Active")).build();
        final var inactiveCourtCase = caseWithDefaults().id(-2L).caseSeq(-2L).caseStatus(new CaseStatus("I", "Inactive")).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
                .courtCases(List.of(activeCourtCase, inactiveCourtCase))
                .build()));

        final var activeOnlyCourtCases = bookingService.getOffenderCourtCases(-1L, true);

        assertThat(activeOnlyCourtCases).containsExactly(CourtCase.builder()
                .id(-1L)
                .caseSeq(-1L)
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
        final var activeCourtCase = caseWithDefaults().id(-1L).caseSeq(-1L).caseStatus(new CaseStatus("A", "Active")).build();
        final var inactiveCourtCase = caseWithDefaults().id(-2L).caseSeq(-2L).caseStatus(new CaseStatus("I", "Inactive")).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
                .courtCases(List.of(activeCourtCase, inactiveCourtCase))
                .build()));

        final var allCourtCases = bookingService.getOffenderCourtCases(-1L, false);

        assertThat(allCourtCases).containsExactly(
                CourtCase.builder()
                        .id(-1L)
                        .caseSeq(-1L)
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
                        .caseSeq(-2L)
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

        when(offenderRepository.findOffenderByNomsId("NomsId")).thenReturn(Optional.of(offender));
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
        when(authenticationFacade.isOverrideRole(any())).thenReturn(true);
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
                        .offenderBooking(OffenderBooking.builder().bookingId(-99L).build())
                        .calculationType(SentenceCalcType.builder().build()
                    ).build()
                )
            );

        final var sentencesAndOffences = bookingService.getSentenceAndOffenceDetails(bookingId);

        assertThat(sentencesAndOffences).containsExactly(
            OffenderSentenceAndOffences.builder()
                .bookingId(-99L)
                .build()
        );
    }

    @Test
    void getSentenceAndOffenceDetails_withFullData() {
        final var bookingId = -1L;
        when(offenderSentenceRepository.findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(bookingId, "%AGG%", "LICENCE"))
            .thenReturn(
                List.of(OffenderSentence.builder()
                        .offenderBooking(OffenderBooking.builder().bookingId(-98L).build())
                        .sequence(2)
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
                                .courtDate(LocalDate.of(2021,1,1))
                                .build()
                            )
                        .terms(List.of(
                            SentenceTerm.builder()
                                .years(2)
                                .sentenceTermCode("IMP")
                                .build(),
                            SentenceTerm.builder()
                                .years(1)
                                .sentenceTermCode("LI")
                                .build()
                        ))
                        .offenderSentenceCharges(List.of(
                            OffenderSentenceCharge.builder()
                                .offenderCharge(OffenderCharge.builder()
                                    .dateOfOffence(LocalDate.of(2021, 1, 2))
                                    .endDate(LocalDate.of(2021, 1, 25))
                                    .offence(Offence.builder()
                                        .offenceIndicators(List.of(
                                            OffenceIndicator.builder().indicatorCode("INDICATOR").build()
                                        ))
                                        .build())
                                    .build()
                                )
                                .build()
                        ))
                        .courtCase(
                            OffenderCourtCase.builder()
                                .caseSeq(10L)
                                .caseInfoNumber("XYZ789")
                                .courtEvents(
                                    List.of(CourtEvent.builder()
                                        .eventDate(LocalDate.of(2021,1,1))
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
                .caseSequence(10L)
                .caseReference("XYZ789")
                .courtDescription("A court")
                .consecutiveToSequence(1)
                .sentenceStatus("A")
                .sentenceCategory("CAT")
                .sentenceCalculationType("CALC")
                .sentenceTypeDescription("Calc description")
                .sentenceDate(LocalDate.of(2021,1,1))
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

    @Nested
    class UpdateLivingUnit {

        final Long SOME_BOOKING_ID = 1L;
        final Long BAD_BOOKING_ID = 2L;
        final Long OLD_LIVING_UNIT_ID = 11L;
        final Long NEW_LIVING_UNIT_ID = 12L;
        final String NEW_LIVING_UNIT_DESC = "Z-1";
        final String SOME_AGENCY_ID = "MDI";
        final String DIFFERENT_AGENCY_ID = "NOT_MDI";

        @Test
        void bookingNotFound_throws() {
            when(offenderBookingRepository.findById(BAD_BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateLivingUnit(BAD_BOOKING_ID, NEW_LIVING_UNIT_DESC))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(valueOf(BAD_BOOKING_ID));

            assertThatThrownBy(() -> bookingService.updateLivingUnit(BAD_BOOKING_ID, aCellSwapLocation()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(valueOf(BAD_BOOKING_ID));
        }

        @Test
        void livingUnitNotFound_throws() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID)).thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(NEW_LIVING_UNIT_DESC);
        }

        @Test
        void livingUnitNotCell_throws() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC))
                    .thenReturn(Optional.of(aLocation(NEW_LIVING_UNIT_ID, SOME_AGENCY_ID, "WING")));

            assertThatThrownBy(() -> bookingService.updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(NEW_LIVING_UNIT_DESC)
                    .hasMessageContaining("WING");
        }

        @Test
        void differentAgency_throws() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC))
                    .thenReturn(Optional.of(aLocation(NEW_LIVING_UNIT_ID, DIFFERENT_AGENCY_ID)));

            assertThatThrownBy(() -> bookingService.updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(SOME_AGENCY_ID)
                    .hasMessageContaining(DIFFERENT_AGENCY_ID);
        }

        @Test
        void ok_updatesRepo() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC))
                    .thenReturn(Optional.of(aLocation(NEW_LIVING_UNIT_ID, SOME_AGENCY_ID)));

            bookingService.updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC);

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

}
