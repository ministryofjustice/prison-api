package net.syscon.elite.service;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtHearingsServiceTest {

    private static final Long OFFENDER_BOOKING_ID = 1L;

    private static final Long COURT_EVENT_ID = 99L;

    private static final PrisonToCourtHearing COURT_HEARING = PrisonToCourtHearing.builder()
            .courtCaseId(-2L)
            .fromPrisonLocation("PRISON")
            .toCourtLocation("COURT")
            .courtHearingDateTime(LocalDateTime.now())
            .build();

    private static final AgencyLocation COURT_LOCATION = AgencyLocation.builder()
            .activeFlag(ActiveFlag.Y)
            .description("Agency Description")
            .id("COURT")
            .type("CRT")
            .build();

    private static final CourtEvent.CourtEventBuilder COURT_EVENT_BUILDER = CourtEvent.builder()
            .courtLocation(COURT_LOCATION)
            .eventDate(LocalDate.EPOCH)
            .startTime(LocalDate.EPOCH.atStartOfDay());

    private static final CourtEvent UN_PERSISTED_COURT_EVENT = COURT_EVENT_BUILDER.build();

    private static final CourtEvent PERSISTED_COURT_EVENT = COURT_EVENT_BUILDER.id(COURT_EVENT_ID).build();

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

    @Mock
    private OffenderBooking offenderBooking;

    @Mock
    private OffenderCourtCase offenderCourtCase;

    @Mock
    private EventType eventType;

    @Mock
    private EventStatus eventStatus;

    private CourtHearingsService courtHearingsService;

    @BeforeEach
    void setup() {
        courtHearingsService = new CourtHearingsService(
                offenderBookingRepository,
                courtEventRepository,
                agencyLocationRepository,
                eventTypeRepository,
                eventStatusRepository);
    }

    @Test
    void scheduleHearing_schedules_a_court_hearing() {
        givenValidBookingCourtCaseAndLocations();

        CourtHearing courtHearing = courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING);

        assertThat(courtHearing).isEqualTo(CourtHearing.builder()
                .id(COURT_EVENT_ID)
                .date(LocalDate.EPOCH)
                .location(AgencyTransformer.transform(COURT_LOCATION))
                .time(LocalDate.EPOCH.atStartOfDay().toLocalTime())
                .build());
    }

    private void givenValidBookingCourtCaseAndLocations() {
        when(offenderBooking.isActive()).thenReturn(true);
        when(offenderBooking.getLocation()).thenReturn(fromPrison);
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(offenderBooking.getCourtCaseBy(COURT_HEARING.getCourtCaseId())).thenReturn(Optional.of(offenderCourtCase));
        when(offenderCourtCase.isActive()).thenReturn(true);
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.of(COURT_LOCATION));
        when(courtEventRepository.save(UN_PERSISTED_COURT_EVENT)).thenReturn(PERSISTED_COURT_EVENT);
        when(eventTypeRepository.findById(EventType.COURT)).thenReturn(Optional.of(eventType));
        when(eventStatusRepository.findById(EventStatus.SCHEDULED)).thenReturn(Optional.of(eventStatus));
    }

    @Test
    void scheduleHearing_errors_when_no_matching_booking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Resource with id [1] not found.");
    }

    @Test
    void scheduleHearing_errors_when_booking_is_not_active() {
        when(offenderBooking.isActive()).thenReturn(false);
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Offender booking with id [1] is not active.");
    }

    @Test
    void scheduleHearing_errors_when_no_matching_court_case_for_booking() {
        when(offenderBooking.isActive()).thenReturn(true);
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(offenderBooking.getCourtCaseBy(COURT_HEARING.getCourtCaseId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Resource with id [-2] not found.");
    }

    @Test
    void scheduleHearing_errors_when_court_case_is_not_active() {
        when(offenderBooking.isActive()).thenReturn(true);
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(offenderBooking.getCourtCaseBy(COURT_HEARING.getCourtCaseId())).thenReturn(Optional.of(offenderCourtCase));
        when(offenderCourtCase.isActive()).thenReturn(false);

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Court case with id [-2] is not active.");
    }

    @Test
    void scheduleHearing_errors_when_prison_not_found() {
        when(offenderBooking.isActive()).thenReturn(true);
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(offenderBooking.getCourtCaseBy(COURT_HEARING.getCourtCaseId())).thenReturn(Optional.of(offenderCourtCase));
        when(offenderCourtCase.isActive()).thenReturn(true);
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Resource with id [PRISON] not found.");
    }

    @Test
    void scheduleHearing_errors_when_court_not_found() {
        when(offenderBooking.isActive()).thenReturn(true);
        when(offenderBooking.getLocation()).thenReturn(fromPrison);
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(offenderBooking.getCourtCaseBy(COURT_HEARING.getCourtCaseId())).thenReturn(Optional.of(offenderCourtCase));
        when(offenderCourtCase.isActive()).thenReturn(true);
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Resource with id [COURT] not found.");
    }

    @Test
    void scheduleHearing_errors_when_supplied_court_is_not_court() {
        when(offenderBooking.isActive()).thenReturn(true);
        when(offenderBooking.getLocation()).thenReturn(fromPrison);
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(offenderBooking.getCourtCaseBy(COURT_HEARING.getCourtCaseId())).thenReturn(Optional.of(offenderCourtCase));
        when(offenderCourtCase.isActive()).thenReturn(true);
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));
        when(agencyLocationRepository.findById("COURT")).thenReturn(Optional.of(fromPrison));
        when(fromPrison.getType()).thenReturn("NOT_CRT");

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supplied court location wih id COURT is not a valid court location.");
    }

    @Test
    void scheduleHearing_errors_when_prison_location_does_not_match_booking() {
        when(offenderBooking.isActive()).thenReturn(true);
        when(offenderBooking.getLocation()).thenReturn(COURT_LOCATION);
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));
        when(offenderBooking.getCourtCaseBy(COURT_HEARING.getCourtCaseId())).thenReturn(Optional.of(offenderCourtCase));
        when(offenderCourtCase.isActive()).thenReturn(true);
        when(agencyLocationRepository.findById("PRISON")).thenReturn(Optional.of(fromPrison));

        assertThatThrownBy(() -> courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Prison location does not match the bookings location.");
    }
}
