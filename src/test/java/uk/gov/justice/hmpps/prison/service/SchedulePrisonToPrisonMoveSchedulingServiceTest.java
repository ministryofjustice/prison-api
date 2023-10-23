package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.SchedulePrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.api.model.ScheduledPrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransferCancellationReason;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.OUT;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule.EventClass.EXT_MOV;

@ExtendWith(MockitoExtension.class)
class SchedulePrisonToPrisonMoveSchedulingServiceTest {

    private static final Long OFFENDER_BOOKING_ID = 1L;

    private static final String FROM_PRISON = "A";

    private static final AgencyLocation FROM_PRISON_AGENCY = AgencyLocation
            .builder().id(FROM_PRISON).description("Prison A description").active(true).build();

    private static final String TO_PRISON = "B";

    private static final AgencyLocation TO_PRISON_AGENCY = AgencyLocation
            .builder().id(TO_PRISON).description("Prison A description").active(true).type(new AgencyLocationType("INST")).build();

    private static final String PRISON_ESCORT_CUSTODY_SERVICES = "PECS";

    private static final OffenderBooking ACTIVE_BOOKING = OffenderBooking
            .builder()
            .active(true)
            .bookingId(OFFENDER_BOOKING_ID)
            .location(AgencyLocation.builder()
                    .active(true)
                    .id(FROM_PRISON)
                    .description("Prison A description")
                    .build())
            .offender(Offender.builder()
                    .nomsId("NOMS_ID")
                    .build())
            .build();

    private static final OffenderBooking INACTIVE_BOOKING = OffenderBooking
            .builder()
            .active(false)
            .bookingId(OFFENDER_BOOKING_ID)
            .location(AgencyLocation.builder()
                    .active(true)
                    .id(FROM_PRISON)
                    .description("Prison A description")
                    .build())
            .offender(Offender.builder()
                    .nomsId("NOMS_ID")
                    .build())
            .build();

    private static final EventStatus SCHEDULED = new EventStatus("SCH", "Schedule approved.");

    private static final EventStatus CANCELLED = new EventStatus("CANC", "Cancelled.");

    private static final String APPOINTMENT = "APP";

    private static final OffenderIndividualSchedule SCHEDULED_APPOINTMENT = OffenderIndividualSchedule
            .builder()
            .id(3L)
            .eventType(APPOINTMENT)
            .eventStatus(SCHEDULED)
            .build();

    private static final String PRISON_TRANSFER = "TRN";

    private final OffenderIndividualSchedule scheduledPrisonMove = OffenderIndividualSchedule
            .builder()
            .id(2L)
            .eventType(PRISON_TRANSFER)
            .eventStatus(SCHEDULED)
            .build();

    private static final String ADMINISTRATIVE_CANCELLATION_REASON = "ADMI";

    private static final TransferCancellationReason CANCELLATION_REASON = new TransferCancellationReason(ADMINISTRATIVE_CANCELLATION_REASON, "cancellation reason");

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    @Mock
    private AgencyLocationRepository agencyLocationRepository;

    @Mock
    private ReferenceCodeRepository<EscortAgencyType> escortAgencyTypeRepository;

    @Mock
    private ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Mock
    private ReferenceCodeRepository<TransferCancellationReason> transferCancellationReasonRepository;

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
                transferCancellationReasonRepository,
                scheduleRepository);
    }

    @Test
    void schedule_move_succeeds() {
        givenAnActiveBooking()
                .andValidToPrison()
                .andEventStatusScheduledFound()
                .andValidEscort();

        final var move = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
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

        assertThat(service.schedule(OFFENDER_BOOKING_ID, move)).isEqualTo(ScheduledPrisonToPrisonMove.builder()
                .id(1L)
                .scheduledMoveDateTime(move.getScheduledMoveDateTime())
                .fromPrisonLocation(AgencyTransformer.transform(FROM_PRISON_AGENCY, false))
                .toPrisonLocation(AgencyTransformer.transform(TO_PRISON_AGENCY, false))
                .build());

        verify(scheduleRepository).save(OffenderIndividualSchedule.builder()
                .eventDate(move.getScheduledMoveDateTime().toLocalDate())
                .startTime(move.getScheduledMoveDateTime())
                .eventClass(EXT_MOV)
                .eventType("TRN")
                .eventSubType("NOTR")
                .eventStatus(SCHEDULED)
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

        final var move = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
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

        final var move = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
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
                .active(false)
                .bookingId(OFFENDER_BOOKING_ID)
                .build()));
    }

    @Test
    void schedule_move_errors_when_move_date_not_in_future() {
        final var moveWithInvalidDate = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, moveWithInvalidDate))
                .hasMessage("Prison to prison move must be in the future.");
    }

    @Test
    void schedule_move_errors_when_from_and_to_are_the_same() {
        final var moveWithInvalidToPrison = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(FROM_PRISON)
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

        final var moveWithInvalidFromPrison = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation("BAD_" + FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
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

        final var move = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
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

        final var move = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
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

        final var move = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
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

        final var move = SchedulePrisonToPrisonMove
                .builder()
                .fromPrisonLocation(FROM_PRISON)
                .toPrisonLocation(TO_PRISON)
                .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
                .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
                .build();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, move))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Escort type PECS for prison to prison move not found.");
    }

    @Test
    void cancel_move_succeeds() {
        givenScheduledMoveWith(ACTIVE_BOOKING)
                .andEventStatusScheduledFound()
                .andEventStatusCancelFound()
                .andTransferCancellationReasonFound();

        assertThat(scheduledPrisonMove.getEventStatus()).isNotEqualTo(CANCELLED);
        assertThat(scheduledPrisonMove.getCancellationReason()).isNotEqualTo(CANCELLATION_REASON);

        service.cancel(ACTIVE_BOOKING.getBookingId(), scheduledPrisonMove.getId(), ADMINISTRATIVE_CANCELLATION_REASON);

        verify(scheduleRepository).save(scheduledPrisonMove);
        assertThat(scheduledPrisonMove.getEventStatus()).isEqualTo(CANCELLED);
        assertThat(scheduledPrisonMove.getCancellationReason()).isEqualTo(CANCELLATION_REASON);
    }

    @Test
    void cancel_move_is_idempotent_if_called_multiple_times() {
        givenScheduledMoveWith(ACTIVE_BOOKING)
                .andEventStatusScheduledFound()
                .andEventStatusCancelFound()
                .andTransferCancellationReasonFound();

        assertThat(scheduledPrisonMove.getEventStatus()).isNotEqualTo(CANCELLED);
        assertThat(scheduledPrisonMove.getCancellationReason()).isNotEqualTo(CANCELLATION_REASON);

        service.cancel(ACTIVE_BOOKING.getBookingId(), scheduledPrisonMove.getId(), ADMINISTRATIVE_CANCELLATION_REASON);
        service.cancel(ACTIVE_BOOKING.getBookingId(), scheduledPrisonMove.getId(), ADMINISTRATIVE_CANCELLATION_REASON);

        verify(scheduleRepository, times(1)).save(scheduledPrisonMove);
        assertThat(scheduledPrisonMove.getEventStatus()).isEqualTo(CANCELLED);
        assertThat(scheduledPrisonMove.getCancellationReason()).isEqualTo(CANCELLATION_REASON);
    }

    @Test
    void cancel_scheduled_move_errors_when_scheduled_move_is_not_a_prison_transfer() {
        given(SCHEDULED_APPOINTMENT);

        assertThatThrownBy(() -> service.cancel(OFFENDER_BOOKING_ID, SCHEDULED_APPOINTMENT.getId(), ADMINISTRATIVE_CANCELLATION_REASON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Scheduled move with id %s not a prison move.", SCHEDULED_APPOINTMENT.getId());
    }

    @Test
    void cancel_scheduled_move_errors_when_scheduled_move_not_found() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(ACTIVE_BOOKING));
        assertThatThrownBy(() -> service.cancel(OFFENDER_BOOKING_ID, 2L, ADMINISTRATIVE_CANCELLATION_REASON))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Scheduled prison move with id %s not found.", 2L);
    }

    @Test
    void cancel_scheduled_move_errors_when_booking_not_associated_with_move() {
        scheduledPrisonMove.setOffenderBooking(ACTIVE_BOOKING);
        when(scheduleRepository.findById(scheduledPrisonMove.getId())).thenReturn(Optional.of(scheduledPrisonMove));
        when(offenderBookingRepository.findById(99L)).thenReturn(Optional.of(ACTIVE_BOOKING));

        assertThatThrownBy(() -> service.cancel(99L, scheduledPrisonMove.getId(), ADMINISTRATIVE_CANCELLATION_REASON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking with id %s not associated with the supplied move id %d.", 99L, scheduledPrisonMove.getId());
    }

    @Test
    void cancel_scheduled_move_errors_when_booking_with_move_not_active() {
        givenScheduledMoveWith(INACTIVE_BOOKING);

        assertThatThrownBy(() -> service.cancel(INACTIVE_BOOKING.getBookingId(), scheduledPrisonMove.getId(), ADMINISTRATIVE_CANCELLATION_REASON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Offender booking for prison to prison move with id %s is not active.", INACTIVE_BOOKING.getBookingId());
    }

    @Test
    void cancel_scheduled_move_errors_when_move_not_in_scheduled_state() {
        givenAnUnscheduledMove()
                .andEventStatusCancelFound()
                .andEventStatusScheduledFound();

        assertThatThrownBy(() -> service.cancel(ACTIVE_BOOKING.getBookingId(), scheduledPrisonMove.getId(), ADMINISTRATIVE_CANCELLATION_REASON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Move with id %s is not in a scheduled state.", scheduledPrisonMove.getId());
    }

    @Test
    void cancel_scheduled_move_errors_when_cancellation_reason_not_known() {
        givenScheduledMoveWith(ACTIVE_BOOKING)
                .andEventStatusCancelFound()
                .andEventStatusScheduledFound();

        assertThatThrownBy(() -> service.cancel(ACTIVE_BOOKING.getBookingId(), scheduledPrisonMove.getId(), "XXXXXX"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cancellation reason XXXXXX not found.");
    }

    private SchedulePrisonToPrisonMoveSchedulingServiceTest givenAnActiveBooking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(ACTIVE_BOOKING));

        return this;
    }

    private void andToPrisonNotFound() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.empty());
    }

    private void andToPrisonNotActive() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.of(AgencyLocation.builder()
                .active(true)
                .id(TO_PRISON)
                .description("Prison B description")
                .active(false)
                .build()));
    }

    private void andToPrisonIsNotPrison() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.of(AgencyLocation.builder()
                .active(true)
                .id(TO_PRISON)
                .description("Prison B description")
                .active(true)
                .type(AgencyLocationType.COURT_TYPE)
                .build()));
    }

    private SchedulePrisonToPrisonMoveSchedulingServiceTest andValidEscort() {
        when(escortAgencyTypeRepository.findById(any())).thenReturn(Optional.of(new EscortAgencyType(PRISON_ESCORT_CUSTODY_SERVICES, "Prison Escort Custody Service")));

        return this;
    }

    private void andEscortNotFound() {
        when(escortAgencyTypeRepository.findById(any())).thenReturn(Optional.empty());

    }

    private SchedulePrisonToPrisonMoveSchedulingServiceTest andValidToPrison() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.of(TO_PRISON_AGENCY));

        return this;
    }

    private SchedulePrisonToPrisonMoveSchedulingServiceTest andEventStatusScheduledFound() {
        when(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED)).thenReturn(Optional.of(SCHEDULED));

        return this;
    }

    private SchedulePrisonToPrisonMoveSchedulingServiceTest andEventStatusCancelFound() {
        when(eventStatusRepository.findById(EventStatus.CANCELLED)).thenReturn(Optional.of(CANCELLED));

        return this;
    }

    private SchedulePrisonToPrisonMoveSchedulingServiceTest givenAnUnscheduledMove() {
        scheduledPrisonMove.setEventStatus(new EventStatus("COMP", "Completed"));
        scheduledPrisonMove.setOffenderBooking(ACTIVE_BOOKING);

        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(ACTIVE_BOOKING));
        when(scheduleRepository.findById(scheduledPrisonMove.getId())).thenReturn(Optional.of(scheduledPrisonMove));

        return this;
    }

    private SchedulePrisonToPrisonMoveSchedulingServiceTest given(final OffenderIndividualSchedule scheduledMove) {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(ACTIVE_BOOKING));
        when(scheduleRepository.findById(scheduledMove.getId())).thenReturn(Optional.of(scheduledMove));
        when(scheduleRepository.findById(scheduledMove.getId())).thenReturn(Optional.of(scheduledMove));

        return this;
    }

    private SchedulePrisonToPrisonMoveSchedulingServiceTest givenScheduledMoveWith(final OffenderBooking booking) {
        scheduledPrisonMove.setOffenderBooking(booking);

        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(ACTIVE_BOOKING));
        when(scheduleRepository.findById(scheduledPrisonMove.getId())).thenReturn(Optional.of(scheduledPrisonMove));

        return this;
    }

    private void andTransferCancellationReasonFound() {
        when(transferCancellationReasonRepository.findById(any())).thenReturn(Optional.of(CANCELLATION_REASON));
    }
}
