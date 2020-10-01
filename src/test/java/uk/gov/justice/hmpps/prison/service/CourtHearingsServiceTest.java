package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.CourtHearing;
import uk.gov.justice.hmpps.prison.api.model.PrisonToCourtHearing;
import uk.gov.justice.hmpps.prison.repository.jpa.model.*;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.time.Instant.ofEpochMilli;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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

    private static final MovementReason EVENT_TYPE = new MovementReason("EVENT_TYPE_CODE", "EVENT_TYPE_DESCRIPTION");

    private static final EventStatus EVENT_STATUS = new EventStatus("EVENT_STATUS_CODE", "EVENT_STATUS_DESCRIPTION");

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    @Mock
    private CourtEventRepository courtEventRepository;

    @Mock
    private AgencyLocationRepository agencyLocationRepository;

    @Mock
    private ReferenceCodeRepository<MovementReason> eventTypeRepository;

    @Mock
    private ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Mock
    private AgencyLocation fromPrison;

    @Mock
    private Offender offender;

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
    void scheduleHearing_for_court_case_schedules_a_court_hearing() {
        givenValidBookingLocationsAndCases(ACTIVE_COURT_CASE);

        assertThat(courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING)).isEqualTo(CourtHearing.builder()
                .id(COURT_EVENT_ID)
                .dateTime(PRISON_TO_COURT_HEARING.getCourtHearingDateTime())
                .location(AgencyTransformer.transform(COURT_LOCATION))
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

    @Test
    void scheduleHearing_no_court_case_schedules_a_court_hearing() {
        givenValidBookingLocationsAndCases();

        assertThat(courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), PRISON_TO_COURT_HEARING)).isEqualTo(CourtHearing.builder()
                .id(COURT_EVENT_ID)
                .dateTime(PRISON_TO_COURT_HEARING.getCourtHearingDateTime())
                .location(AgencyTransformer.transform(COURT_LOCATION))
                .build());

        verify(courtEventRepository).save(CourtEvent.builder()
                .courtLocation(COURT_LOCATION)
                .courtEventType(EVENT_TYPE)
                .directionCode("OUT")
                .eventDate(PRISON_TO_COURT_HEARING.getCourtHearingDateTime().toLocalDate())
                .eventStatus(EVENT_STATUS)
                .offenderBooking(offenderBooking)
                .startTime(PRISON_TO_COURT_HEARING.getCourtHearingDateTime())
                .commentText(PRISON_TO_COURT_HEARING.getComments())
                .build());
    }

    private void givenValidBookingLocationsAndCases(final OffenderCourtCase... cases) {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(fromPrison)
                .courtCases(List.of(cases))
                .offender(offender)
                .build();

        when(offender.getId()).thenReturn(-1L);
        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.of(COURT_LOCATION));
        when(courtEventRepository.save(any())).thenReturn(PERSISTED_COURT_EVENT);
        when(eventTypeRepository.findById(MovementReason.COURT)).thenReturn(Optional.of(EVENT_TYPE));
        when(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED)).thenReturn(Optional.of(EVENT_STATUS));
    }

    @Test
    void scheduleHearing_for_court_case_errors_when_no_matching_booking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Offender booking with id %d not found.", OFFENDER_BOOKING_ID);
    }

    @Test
    void scheduleHearing_no_court_case_errors_when_no_matching_booking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Offender booking with id %d not found.", OFFENDER_BOOKING_ID);
    }

    @Test
    void scheduleHearing_for_court_case_errors_when_booking_is_not_active() {
        givenNoActiveBooking();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Offender booking with id %d is not active.", offenderBooking.getBookingId());
    }

    @Test
    void scheduleHearing_no_court_case_errors_when_booking_is_not_active() {
        givenNoActiveBooking();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Offender booking with id %d is not active.", offenderBooking.getBookingId());
    }

    private void givenNoActiveBooking() {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("N")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(fromPrison)
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
    }

    @Test
    void scheduleHearing_for_court_case_errors_when_no_matching_court_case_for_booking() {
        givenNoMatchingCourtCaseForActiveBooking();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court case with id 1 not found.");
    }

    private void givenNoMatchingCourtCaseForActiveBooking() {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(fromPrison)
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
    }

    @Test
    void scheduleHearing_for_court_case_errors_when_court_case_is_not_active() {
        givenNoActiveCourtCase();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Court case with id 1 is not active.");
    }

    private void givenNoActiveCourtCase() {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(fromPrison)
                .courtCases(List.of(OffenderCourtCase.builder()
                        .id(1L)
                        .caseStatus(INACTIVE_CASE_STATUS)
                        .build()))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
    }

    @Test
    void scheduleHearing_for_court_case_errors_when_prison_not_found() {
        givenPrisonNotFound();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Prison with id PRISON not found.");
    }

    @Test
    void scheduleHearing_no_court_case_errors_when_prison_not_found() {
        givenPrisonNotFound();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Prison with id PRISON not found.");
    }

    private void givenPrisonNotFound() {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(fromPrison)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.empty());
    }

    @Test
    void scheduleHearing_for_court_case_errors_when_court_not_found() {
        givenCourtNotFound();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court with id COURT not found.");
    }

    @Test
    void scheduleHearing_no_court_case_errors_when_court_not_found() {
        givenCourtNotFound();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(offenderBooking.getBookingId(), PRISON_TO_COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court with id COURT not found.");
    }

    private void givenCourtNotFound() {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(fromPrison)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.empty());
    }

    @Test
    void scheduleHearing_for_court_case_errors_when_court_is_not_currently_active() {
        givenProvidedCourtIsNotActive();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Supplied court location wih id %s is not active.", PRISON_TO_COURT_HEARING.getToCourtLocation());
    }

    @Test
    void scheduleHearing_no_court_case_errors_when_court_is_not_currently_active() {
        givenProvidedCourtIsNotActive();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Supplied court location wih id %s is not active.", PRISON_TO_COURT_HEARING.getToCourtLocation());
    }

    private void givenProvidedCourtIsNotActive() {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
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
    void scheduleHearing_for_court_case_errors_when_supplied_court_is_not_court() {
        givenProvidedCourtLocationIsNotACourt();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supplied court location wih id COURT is not a valid court location.");
    }

    @Test
    void scheduleHearing_no_court_case_errors_when_supplied_court_is_not_court() {
        givenProvidedCourtLocationIsNotACourt();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supplied court location wih id COURT is not a valid court location.");
    }

    private void givenProvidedCourtLocationIsNotACourt() {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(fromPrison)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.of(fromPrison));
        when(fromPrison.getType()).thenReturn("NOT_CRT");
    }

    @Test
    void scheduleHearing_no_court_case_errors_when_prison_location_does_not_match_booking() {
        givenPrisonDoesNotMatchTheBooking();

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, PRISON_TO_COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Prison location does not match the bookings location.");
    }

    private void givenPrisonDoesNotMatchTheBooking() {
        offenderBooking = OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(COURT_LOCATION)
                .courtCases(List.of(ACTIVE_COURT_CASE))
                .build();

        when(offenderBookingRepository.findById(offenderBooking.getBookingId())).thenReturn(Optional.of(offenderBooking));
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
    }

    @Test
    void scheduleHearing_for_court_case_errors_when_hearing_date_not_in_future() {
        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, 1L, PrisonToCourtHearing.builder()
                .courtHearingDateTime(LocalDateTime.now(startOfEpochClock))
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Court hearing must be in the future.");
    }

    @Test
    void scheduleHearing_no_court_case_errors_when_hearing_date_not_in_future() {
        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, PrisonToCourtHearing.builder()
                .courtHearingDateTime(LocalDateTime.now(startOfEpochClock))
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Court hearing must be in the future.");
    }

    @Test
    void getCourtHearingsFor_retrieves_single_hearing_for_booking() {
        final var hearing = CourtEvent.builder()
                .id(1L)
                .courtLocation(COURT_LOCATION)
                .eventDate(PRISON_TO_COURT_HEARING.getCourtHearingDateTime().toLocalDate())
                .offenderBooking(offenderBooking)
                .offenderCourtCase(ACTIVE_COURT_CASE)
                .startTime(PRISON_TO_COURT_HEARING.getCourtHearingDateTime())
                .build();

        givenValidBookingWithOneOrMoreCourtHearings(1L, hearing(hearing.getId()));

        final var hearings = courtHearingsService.getCourtHearingsFor(1L, null, null);

        assertThat(hearings.getHearings())
                .containsExactly(
                        CourtHearing.builder()
                                .id(hearing.getId())
                                .dateTime(hearing.getEventDateTime())
                                .location(AgencyTransformer.transform(hearing.getCourtLocation()))
                                .build());
    }

    @Test
    void getCourtHearingsFor_retrieves_multiple_hearings_for_booking() {
        final var hearing1 = CourtEvent.builder()
                .id(1L)
                .courtLocation(COURT_LOCATION)
                .eventDate(PRISON_TO_COURT_HEARING.getCourtHearingDateTime().toLocalDate())
                .offenderBooking(offenderBooking)
                .offenderCourtCase(ACTIVE_COURT_CASE)
                .startTime(PRISON_TO_COURT_HEARING.getCourtHearingDateTime())
                .build();

        final var hearing2 = CourtEvent.builder()
                .id(2L)
                .courtLocation(COURT_LOCATION)
                .eventDate(PRISON_TO_COURT_HEARING.getCourtHearingDateTime().toLocalDate())
                .offenderBooking(offenderBooking)
                .offenderCourtCase(ACTIVE_COURT_CASE)
                .startTime(PRISON_TO_COURT_HEARING.getCourtHearingDateTime())
                .build();

        givenValidBookingWithOneOrMoreCourtHearings(2L, hearing(hearing1.getId()), hearing(hearing2.getId()));

        final var hearings = courtHearingsService.getCourtHearingsFor(2L, null, null);

        assertThat(hearings.getHearings())
                .containsExactly(
                        CourtHearing.builder()
                                .id(hearing1.getId())
                                .dateTime(hearing1.getEventDateTime())
                                .location(AgencyTransformer.transform(hearing1.getCourtLocation()))
                                .build(),
                        CourtHearing.builder()
                                .id(hearing2.getId())
                                .dateTime(hearing2.getEventDateTime())
                                .location(AgencyTransformer.transform(hearing2.getCourtLocation()))
                                .build()
                );
    }

    @Test
    void getCourtHearings_throws_service_exception_for_invalid_dates() {
        assertThatThrownBy(() -> courtHearingsService.getCourtHearingsFor(-1L, LocalDate.of(2020, 3, 23), LocalDate.of(2020, 3, 22)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid date range: toDate is before fromDate.");
    }

    @Test
    void getCourtHearings_does_not_throw_service_exception_for_valid_dates() {
        assertThatCode(() -> courtHearingsService.getCourtHearingsFor(-1L, LocalDate.of(2020, 3, 22), LocalDate.of(2020, 3, 23)))
                .doesNotThrowAnyException();

        assertThatCode(() -> courtHearingsService.getCourtHearingsFor(-1L, LocalDate.of(2020, 3, 22), LocalDate.of(2020, 3, 22)))
                .doesNotThrowAnyException();
    }

    private void givenValidBookingWithOneOrMoreCourtHearings(final Long bookingId, final CourtEvent... events) {
        when(courtEventRepository.findAll(CourtEventFilter.builder().bookingId(bookingId).build())).thenReturn(asList(events));
    }

    private CourtEvent hearing(final Long hearingId) {
        return CourtEvent.builder()
                .id(hearingId)
                .courtLocation(COURT_LOCATION)
                .eventDate(PRISON_TO_COURT_HEARING.getCourtHearingDateTime().toLocalDate())
                .offenderBooking(offenderBooking)
                .offenderCourtCase(ACTIVE_COURT_CASE)
                .startTime(PRISON_TO_COURT_HEARING.getCourtHearingDateTime())
                .build();
    }
}
