package net.syscon.elite.service;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.CaseStatus;
import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.EventType;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.model.OffenderCourtCase;
import net.syscon.elite.repository.jpa.repository.AgencyLocationRepository;
import net.syscon.elite.repository.jpa.repository.CourtEventRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.repository.jpa.repository.ReferenceCodeRepository;
import net.syscon.elite.service.transformers.AgencyTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtHearingsServiceTest {

    private static final Long OFFENDER_BOOKING_ID = 1L;

    private static final Long COURT_EVENT_ID = 99L;

    private static final PrisonToCourtHearing PRISON_TO_COURT_HEARING = PrisonToCourtHearing.builder()
            .fromPrisonLocation("PRISON")
            .toCourtLocation("COURT")
            .courtHearingDateTime(LocalDateTime.of(2020, 3, 13, 12, 0))
            .comments("some comments related to the court hearing.")
            .build();

    private static final AgencyLocation COURT_LOCATION = AgencyLocation.builder()
            .activeFlag(ActiveFlag.Y)
            .description("Agency Description")
            .id("COURT")
            .type("CRT")
            .build();

    private static final CourtEvent PERSISTED_COURT_EVENT = CourtEvent.builder()
            .id(COURT_EVENT_ID)
            .courtLocation(COURT_LOCATION)
            .eventDate(LocalDate.of(2020, 3, 13))
            .startTime(LocalDateTime.of(2020, 3, 13, 12, 0))
            .build();

    private static final CaseStatus ACTIVE_CASE_STATUS = new CaseStatus("A", "Active");

    private static final CaseStatus INACTIVE_CASE_STATUS = new CaseStatus("I", "Inactive");

    private static final OffenderCourtCase ACTIVE_COURT_CASE = OffenderCourtCase.builder()
            .id(1L)
            .caseSeq(1L)
            .caseStatus(ACTIVE_CASE_STATUS)
            .build();

    private static final EventType EVENT_TYPE = new EventType("EVENT_TYPE_CODE", "EVENT_TYPE_DESCRIPTION");

    private static final EventStatus EVENT_STATUS = new EventStatus("EVENT_STATUS_CODE", "EVENT_STATUS_DESCRIPTION");

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    @Mock
    private CourtEventRepository courtEventRepository;

    @Mock
    private AgencyLocationRepository agencyLocationRepository;

    @Mock
    private ReferenceCodeRepository<EventType> eventTypeRepository;

    @Mock
    private ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Mock
    private AgencyLocation fromPrison;

    private final Clock startOfEpochClock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());

    private OffenderBooking offenderBooking;

    private CourtHearingsService courtHearingsService;

    @BeforeEach
    void setup() {
        courtHearingsService = new CourtHearingsService(
                offenderBookingRepository,
                courtEventRepository,
                agencyLocationRepository,
                eventTypeRepository,
                eventStatusRepository,
                startOfEpochClock);
    }

    @Test
    void scheduleHearing_schedules_a_court_hearing() {
        givenValidBookingCourtCaseAndLocations();

        assertThat(courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING)).isEqualTo(CourtHearing.builder()
                .id(COURT_EVENT_ID)
                .date(PRISON_TO_COURT_HEARING.getCourtHearingDateTime().toLocalDate())
                .location(AgencyTransformer.transform(COURT_LOCATION))
                .time(PRISON_TO_COURT_HEARING.getCourtHearingDateTime().toLocalTime())
                .build());

        verify(courtEventRepository).save(CourtEvent.builder()
                .courtLocation(COURT_LOCATION)
                .courtEventType(EVENT_TYPE)
                .directionCode("OUT")
                .eventDate(PRISON_TO_COURT_HEARING.getCourtHearingDateTime().toLocalDate())
                .eventStatus(EVENT_STATUS)
                .offenderBooking(offenderBooking)
                .offenderCourtCase(ACTIVE_COURT_CASE)
                .startTime(PRISON_TO_COURT_HEARING.getCourtHearingDateTime())
                .commentText(PRISON_TO_COURT_HEARING.getComments())
                .build());
    }

    private void givenValidBookingCourtCaseAndLocations() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .bookingSequence(1)
                .location(fromPrison)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.of(COURT_LOCATION));
        when(courtEventRepository.save(any())).thenReturn(PERSISTED_COURT_EVENT);
        when(eventTypeRepository.findById(EventType.COURT)).thenReturn(Optional.of(EVENT_TYPE));
        when(eventStatusRepository.findById(EventStatus.SCHEDULED)).thenReturn(Optional.of(EVENT_STATUS));
    }

    @Test
    void scheduleHearing_errors_when_no_matching_booking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Resource with id [%s] not found.", OFFENDER_BOOKING_ID);
    }

    @Test
    void scheduleHearing_errors_when_booking_is_not_active() {
        givenNoActiveBooking();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Offender booking with id %s is not active.", OFFENDER_BOOKING_ID);
    }

    private void givenNoActiveBooking() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .location(fromPrison)
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
    }

    @Test
    void scheduleHearing_errors_when_no_matching_court_case_for_booking() {
        givenNoMatchingCourtCaseForActiveBooking();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Resource with id [1] not found.");
    }

    private void givenNoMatchingCourtCaseForActiveBooking() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .bookingSequence(1)
                .location(fromPrison)
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
    }

    @Test
    void scheduleHearing_errors_when_court_case_is_not_active() {
        givenNoActiveCourtCase();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Court case with id 1 is not active.");
    }

    private void givenNoActiveCourtCase() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .bookingSequence(1)
                .location(fromPrison)
                .courtCases(List.of(OffenderCourtCase.builder()
                        .id(1L)
                        .caseStatus(INACTIVE_CASE_STATUS)
                        .build()))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
    }

    @Test
    void scheduleHearing_errors_when_prison_not_found() {
        givenPrisonNotFound();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Resource with id [PRISON] not found.");
    }

    private void givenPrisonNotFound() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .bookingSequence(1)
                .location(fromPrison)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.empty());
    }

    @Test
    void scheduleHearing_errors_when_court_not_found() {
        givenCourtNotFound();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Resource with id [COURT] not found.");
    }

    private void givenCourtNotFound() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .bookingSequence(1)
                .location(fromPrison)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.empty());
    }

    @Test
    void scheduleHearing_errors_when_court_is_not_currently_active() {
        givenProvidedCourtIsNotActive();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Supplied court location wih id %s is not active.", PRISON_TO_COURT_HEARING.getToCourtLocation());
    }

    private void givenProvidedCourtIsNotActive() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .bookingSequence(1)
                .location(fromPrison)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.of(AgencyLocation.builder()
                .activeFlag(ActiveFlag.N)
                .description("Agency Description")
                .id("COURT")
                .type("CRT")
                .build()));
    }

    @Test
    void scheduleHearing_errors_when_supplied_court_is_not_court() {
        givenProvidedCourtLocationIsNotACourt();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supplied court location wih id COURT is not a valid court location.");
    }

    private void givenProvidedCourtLocationIsNotACourt() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .bookingSequence(1)
                .location(fromPrison)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.of(fromPrison));
        when(fromPrison.getType()).thenReturn("NOT_CRT");
    }

    @Test
    void scheduleHearing_errors_when_prison_location_does_not_match_booking() {
        givenPrisonDoesNotMatchTheBooking();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Prison location does not match the bookings location.");
    }

    private void givenPrisonDoesNotMatchTheBooking() {
        offenderBooking = OffenderBooking
                .builder()
                .bookingId(OFFENDER_BOOKING_ID)
                .bookingSequence(1)
                .location(COURT_LOCATION)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
    }

    @Test
    void scheduleHearing_errors_when_hearing_date_not_in_future() {
        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PrisonToCourtHearing.builder()
                .courtHearingDateTime(LocalDateTime.now(startOfEpochClock))
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Court hearing must be in the future.");
    }
}
