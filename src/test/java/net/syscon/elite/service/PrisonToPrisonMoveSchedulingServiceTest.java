package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonToPrisonMove;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.EscortAgencyType;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.Offender;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.model.OffenderIndividualSchedule;
import net.syscon.elite.repository.jpa.repository.AgencyLocationRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.repository.jpa.repository.OffenderIndividualScheduleRepository;
import net.syscon.elite.repository.jpa.repository.ReferenceCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.Instant.ofEpochMilli;
import static net.syscon.elite.repository.jpa.model.MovementDirection.OUT;
import static net.syscon.elite.repository.jpa.model.OffenderIndividualSchedule.EventClass.EXT_MOV;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrisonToPrisonMoveSchedulingServiceTest {

    private static final Long OFFENDER_BOOKING_ID = 1L;

    private static final String FROM_PRISON = "A";

    private static final AgencyLocation FROM_PRISON_AGENCY = AgencyLocation
            .builder().id(FROM_PRISON).description("Prison A description").activeFlag(ActiveFlag.Y).build();

    private static final String TO_PRISON = "B";

    private static final AgencyLocation TO_PRISON_AGENCY = AgencyLocation
            .builder().id(TO_PRISON).description("Prison A description").activeFlag(ActiveFlag.Y).type("INST").build();

    private static final String PRISON_ESCORT_CUSTODY_SERVICES = "PECS";

    private static final OffenderBooking ACTIVE_BOOKING = OffenderBooking
            .builder()
            .activeFlag("Y")
            .bookingId(OFFENDER_BOOKING_ID)
            .location(AgencyLocation.builder()
                    .activeFlag(ActiveFlag.Y)
                    .id(FROM_PRISON)
                    .description("Prison A description")
                    .build())
            .offender(Offender.builder()
                    .nomsId("NOMS_ID")
                    .build())
            .build();

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    @Mock
    private AgencyLocationRepository agencyLocationRepository;

    @Mock
    private ReferenceCodeRepository<EscortAgencyType> escortAgencyTypeRepository;

    @Mock
    private ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Mock
    private OffenderIndividualScheduleRepository scheduleRepository;

    private PrisonToPrisonMoveSchedulingService service;

    private final Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());

    @BeforeEach
    void setup() {
        service = new PrisonToPrisonMoveSchedulingService(
                clock,
                offenderBookingRepository,
                agencyLocationRepository,
                escortAgencyTypeRepository,
                eventStatusRepository,
                scheduleRepository);
    }

    @Test
    void schedule_move_succeeds() {
        givenAnActiveBooking()
                .andValidToPrison()
                .andEventStatusScheduledFound()
                .andValidEscort();

        final var move = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        when(scheduleRepository.save(any())).thenReturn(OffenderIndividualSchedule.builder()
                .id(1L)
                .eventDate(move.getScheduledMoveDateTime().toLocalDate())
                .startTime(move.getScheduledMoveDateTime())
                .eventClass(EXT_MOV)
                .eventType("TRN")
                .eventSubType("NOTR")
                .eventStatus(new EventStatus("SCH", "Scheduled"))
                .escortAgencyType(new EscortAgencyType(PRISON_ESCORT_CUSTODY_SERVICES, "Prison Escort Custody Service"))
                .fromLocation(FROM_PRISON_AGENCY)
                .toLocation(TO_PRISON_AGENCY)
                .movementDirection(OUT)
                .offenderBooking(ACTIVE_BOOKING)
                .build());

        // TODO - assert on return model object when defined.

        service.schedule(OFFENDER_BOOKING_ID, move);

        verify(scheduleRepository).save(OffenderIndividualSchedule.builder()
                .eventDate(move.getScheduledMoveDateTime().toLocalDate())
                .startTime(move.getScheduledMoveDateTime())
                .eventClass(EXT_MOV)
                .eventType("TRN")
                .eventSubType("NOTR")
                .eventStatus(new EventStatus("SCH", "Scheduled"))
                .escortAgencyType(new EscortAgencyType(PRISON_ESCORT_CUSTODY_SERVICES, "Prison Escort Custody Service"))
                .fromLocation(FROM_PRISON_AGENCY)
                .toLocation(TO_PRISON_AGENCY)
                .movementDirection(OUT)
                .offenderBooking(ACTIVE_BOOKING)
                .build());
    }

    @Test
    void schedule_move_errors_when_no_matching_booking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.empty());

        final var move = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, move))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Offender booking with id %d not found.", OFFENDER_BOOKING_ID);
    }

    @Test
    void schedule_move_errors_when_booking_is_not_active() {
        givenAnInActiveBooking();

        final var move = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, move))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Offender booking for prison to prison move with id %d is not active.", OFFENDER_BOOKING_ID);
    }

    private void givenAnInActiveBooking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(OffenderBooking
                .builder()
                .activeFlag("N")
                .bookingId(OFFENDER_BOOKING_ID)
                .build()));
    }

    @Test
    void schedule_move_errors_when_move_date_not_in_future() {
        final var moveWithInvalidDate = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, moveWithInvalidDate))
                .hasMessage("Prison to prison move must be in the future.");
    }

    @Test
    void schedule_move_errors_when_from_and_to_are_the_same() {
        final var moveWithInvalidToPrison = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(FROM_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, moveWithInvalidToPrison))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prison to prison move from and to prisons cannot be the same.");
    }

    @Test
    void schedule_move_errors_when_from_does_not_match_offenders_booking() {
        givenAnActiveBooking();

        final var moveWithInvalidFromPrison = PrisonToPrisonMove
                .builder()
                .fromPrison("BAD_" + FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, moveWithInvalidFromPrison))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prison to prison move from prison does not match that of the booking.");
    }

    @Test
    void schedule_move_errors_when_to_prison_not_found() {
        givenAnActiveBooking()
                .andValidEscort()
                .andToPrisonNotFound();

        final var move = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, move))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Prison with id %s not found.", TO_PRISON);
    }

    @Test
    void schedule_move_errors_when_to_prison_not_active() {
        givenAnActiveBooking()
                .andValidEscort()
                .andToPrisonNotActive();

        final var move = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, move))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prison with id %s not active.", TO_PRISON);
    }

    @Test
    void schedule_move_errors_when_to_prison_is_not_prison() {
        givenAnActiveBooking()
                .andValidEscort()
                .andToPrisonIsNotPrison();

        final var move = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, move))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prison to prison move to prison is not a prison.");
    }

    @Test
    void schedule_move_errors_when_active_escort_agency_not_found() {
        givenAnActiveBooking()
                .andEscortNotFound();

        final var move = PrisonToPrisonMove
                .builder()
                .fromPrison(FROM_PRISON)
                .toPrison(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, move))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Escort type PECS for prison to prison move not found.");
    }

    @Disabled
    @Test
    void schedule_move_errors_when_existing_active_schedule() {
        // TODO - DT-780
    }

    private PrisonToPrisonMoveSchedulingServiceTest givenAnActiveBooking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(ACTIVE_BOOKING));

        return this;
    }

    private void andToPrisonNotFound() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.empty());
    }

    private void andToPrisonNotActive() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.of(AgencyLocation.builder()
                .activeFlag(ActiveFlag.Y)
                .id(TO_PRISON)
                .description("Prison B description")
                .activeFlag(ActiveFlag.N)
                .build()));
    }

    private void andToPrisonIsNotPrison() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.of(AgencyLocation.builder()
                .activeFlag(ActiveFlag.Y)
                .id(TO_PRISON)
                .description("Prison B description")
                .activeFlag(ActiveFlag.Y)
                .type("CRT")
                .build()));
    }

    private PrisonToPrisonMoveSchedulingServiceTest andValidEscort() {
        when(escortAgencyTypeRepository.findById(any())).thenReturn(Optional.of(new EscortAgencyType(PRISON_ESCORT_CUSTODY_SERVICES, "Prison Escort Custody Service")));

        return this;
    }

    private void andEscortNotFound() {
        when(escortAgencyTypeRepository.findById(any())).thenReturn(Optional.empty());

    }

    private PrisonToPrisonMoveSchedulingServiceTest andValidToPrison() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.of(TO_PRISON_AGENCY));

        return this;
    }

    private PrisonToPrisonMoveSchedulingServiceTest andEventStatusScheduledFound() {
        when(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED)).thenReturn(Optional.of(new EventStatus("SCH", "Scheduled")));

        return this;
    }
}