package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.SchedulePrisonToPrisonMove
import uk.gov.justice.hmpps.prison.api.model.ScheduledPrisonToPrisonMove
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule.EventClass
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransferCancellationReason
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional

internal class SchedulePrisonToPrisonMoveSchedulingServiceTest {
  private val scheduledPrisonMove: OffenderIndividualSchedule = OffenderIndividualSchedule
    .builder()
    .id(2L)
    .eventType(PRISON_TRANSFER)
    .eventStatus(SCHEDULED)
    .build()

  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val agencyLocationRepository: AgencyLocationRepository = mock()
  private val escortAgencyTypeRepository: ReferenceCodeRepository<EscortAgencyType> = mock()
  private val eventStatusRepository: ReferenceCodeRepository<EventStatus> = mock()
  private val transferCancellationReasonRepository: ReferenceCodeRepository<TransferCancellationReason> = mock()
  private val scheduleRepository: OffenderIndividualScheduleRepository = mock()

  private val clock: Clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())

  private val service = PrisonToPrisonMoveSchedulingService(
    clock,
    offenderBookingRepository,
    agencyLocationRepository,
    escortAgencyTypeRepository,
    eventStatusRepository,
    transferCancellationReasonRepository,
    scheduleRepository,
  )

  @Test
  fun schedule_move_succeeds() {
    givenAnActiveBooking()
      .andValidToPrison()
      .andEventStatusScheduledFound()
      .andValidEscort()

    val move = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    whenever(scheduleRepository.save<OffenderIndividualSchedule>(any()))
      .thenReturn(
        OffenderIndividualSchedule.builder()
          .id(1L)
          .eventDate(move.scheduledMoveDateTime.toLocalDate())
          .startTime(move.scheduledMoveDateTime)
          .eventClass(EventClass.EXT_MOV)
          .eventType("TRN")
          .eventSubType("NOTR")
          .eventStatus(EventStatus("SCH", "Scheduled"))
          .escortAgencyType(EscortAgencyType(PRISON_ESCORT_CUSTODY_SERVICES, "Prison Escort Custody Service"))
          .fromLocation(FROM_PRISON_AGENCY)
          .toLocation(TO_PRISON_AGENCY)
          .movementDirection(MovementDirection.OUT)
          .offenderBooking(ACTIVE_BOOKING)
          .build(),
      )

    assertThat(service.schedule(OFFENDER_BOOKING_ID, move)).isEqualTo(
      ScheduledPrisonToPrisonMove.builder()
        .id(1L)
        .scheduledMoveDateTime(move.scheduledMoveDateTime)
        .fromPrisonLocation(AgencyTransformer.transform(FROM_PRISON_AGENCY, false, false))
        .toPrisonLocation(AgencyTransformer.transform(TO_PRISON_AGENCY, false, false))
        .build(),
    )

    verify(scheduleRepository).save(
      OffenderIndividualSchedule.builder()
        .eventDate(move.scheduledMoveDateTime.toLocalDate())
        .startTime(move.scheduledMoveDateTime)
        .eventClass(EventClass.EXT_MOV)
        .eventType("TRN")
        .eventSubType("NOTR")
        .eventStatus(SCHEDULED)
        .escortAgencyType(EscortAgencyType(PRISON_ESCORT_CUSTODY_SERVICES, "Prison Escort Custody Service"))
        .fromLocation(FROM_PRISON_AGENCY)
        .toLocation(TO_PRISON_AGENCY)
        .movementDirection(MovementDirection.OUT)
        .offenderBooking(ACTIVE_BOOKING)
        .build(),
    )
  }

  @Test
  fun schedule_move_errors_when_no_matching_booking() {
    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.empty<OffenderBooking>(),
    )

    val move = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    assertThatThrownBy { service.schedule(OFFENDER_BOOKING_ID, move) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Offender booking with id %d not found.", OFFENDER_BOOKING_ID)
  }

  @Test
  fun schedule_move_errors_when_booking_is_not_active() {
    givenAnInActiveBooking()

    val move = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    assertThatThrownBy { service.schedule(OFFENDER_BOOKING_ID, move) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Offender booking for prison to prison move with id %d is not active.", OFFENDER_BOOKING_ID)
  }

  private fun givenAnInActiveBooking() {
    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.of(
        OffenderBooking
          .builder()
          .active(false)
          .bookingId(OFFENDER_BOOKING_ID)
          .build(),
      ),
    )
  }

  @Test
  fun schedule_move_errors_when_move_date_not_in_future() {
    val moveWithInvalidDate = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock))
      .build()

    assertThatThrownBy {
      service.schedule(
        OFFENDER_BOOKING_ID,
        moveWithInvalidDate,
      )
    }
      .hasMessage("Prison to prison move must be in the future.")
  }

  @Test
  fun schedule_move_errors_when_from_and_to_are_the_same() {
    val moveWithInvalidToPrison = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(FROM_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    assertThatThrownBy {
      service.schedule(
        OFFENDER_BOOKING_ID,
        moveWithInvalidToPrison,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Prison to prison move from and to prisons cannot be the same.")
  }

  @Test
  fun schedule_move_errors_when_from_does_not_match_offenders_booking() {
    givenAnActiveBooking()

    val moveWithInvalidFromPrison = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation("BAD_" + FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    assertThatThrownBy {
      service.schedule(
        OFFENDER_BOOKING_ID,
        moveWithInvalidFromPrison,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Prison to prison move from prison does not match that of the booking.")
  }

  @Test
  fun schedule_move_errors_when_to_prison_not_found() {
    givenAnActiveBooking()
      .andValidEscort()
      .andToPrisonNotFound()

    val move = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    assertThatThrownBy { service.schedule(OFFENDER_BOOKING_ID, move) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Prison with id %s not found.", TO_PRISON)
  }

  @Test
  fun schedule_move_errors_when_to_prison_not_active() {
    givenAnActiveBooking()
      .andValidEscort()
      .andToPrisonNotActive()

    val move = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    assertThatThrownBy { service.schedule(OFFENDER_BOOKING_ID, move) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Prison with id %s not active.", TO_PRISON)
  }

  @Test
  fun schedule_move_errors_when_to_prison_is_not_prison() {
    givenAnActiveBooking()
      .andValidEscort()
      .andToPrisonIsNotPrison()

    val move = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    assertThatThrownBy { service.schedule(OFFENDER_BOOKING_ID, move) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Prison to prison move to prison is not a prison.")
  }

  @Test
  fun schedule_move_errors_when_active_escort_agency_not_found() {
    givenAnActiveBooking()
      .andEscortNotFound()

    val move = SchedulePrisonToPrisonMove
      .builder()
      .fromPrisonLocation(FROM_PRISON)
      .toPrisonLocation(TO_PRISON)
      .escortType(PRISON_ESCORT_CUSTODY_SERVICES)
      .scheduledMoveDateTime(LocalDateTime.now(clock).plusDays(1))
      .build()

    assertThatThrownBy { service.schedule(OFFENDER_BOOKING_ID, move) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Escort type PECS for prison to prison move not found.")
  }

  @Test
  fun cancel_move_succeeds() {
    givenScheduledMoveWith(ACTIVE_BOOKING)
      .andEventStatusScheduledFound()
      .andEventStatusCancelFound()
      .andTransferCancellationReasonFound()

    assertThat(scheduledPrisonMove.eventStatus).isNotEqualTo(CANCELLED)
    assertThat(scheduledPrisonMove.cancellationReason).isNotEqualTo(
      CANCELLATION_REASON,
    )

    service.cancel(ACTIVE_BOOKING.bookingId, scheduledPrisonMove.id, ADMINISTRATIVE_CANCELLATION_REASON)

    verify(scheduleRepository)
      .save(scheduledPrisonMove)
    assertThat(scheduledPrisonMove.eventStatus).isEqualTo(CANCELLED)
    assertThat(scheduledPrisonMove.cancellationReason).isEqualTo(
      CANCELLATION_REASON,
    )
  }

  @Test
  fun cancel_move_is_idempotent_if_called_multiple_times() {
    givenScheduledMoveWith(ACTIVE_BOOKING)
      .andEventStatusScheduledFound()
      .andEventStatusCancelFound()
      .andTransferCancellationReasonFound()

    assertThat(scheduledPrisonMove.eventStatus).isNotEqualTo(CANCELLED)
    assertThat(scheduledPrisonMove.cancellationReason).isNotEqualTo(CANCELLATION_REASON)

    service.cancel(ACTIVE_BOOKING.bookingId, scheduledPrisonMove.id, ADMINISTRATIVE_CANCELLATION_REASON)
    service.cancel(ACTIVE_BOOKING.bookingId, scheduledPrisonMove.id, ADMINISTRATIVE_CANCELLATION_REASON)

    verify(scheduleRepository).save(scheduledPrisonMove)
    assertThat(scheduledPrisonMove.eventStatus).isEqualTo(CANCELLED)
    assertThat(scheduledPrisonMove.cancellationReason).isEqualTo(CANCELLATION_REASON)
  }

  @Test
  fun cancel_scheduled_move_errors_when_scheduled_move_is_not_a_prison_transfer() {
    given(SCHEDULED_APPOINTMENT)

    assertThatThrownBy {
      service.cancel(
        OFFENDER_BOOKING_ID,
        SCHEDULED_APPOINTMENT.id,
        ADMINISTRATIVE_CANCELLATION_REASON,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Scheduled move with id %s not a prison move.", SCHEDULED_APPOINTMENT.id)
  }

  @Test
  fun cancel_scheduled_move_errors_when_scheduled_move_not_found() {
    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.of<OffenderBooking>(ACTIVE_BOOKING),
    )
    assertThatThrownBy {
      service.cancel(
        OFFENDER_BOOKING_ID,
        2L,
        ADMINISTRATIVE_CANCELLATION_REASON,
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Scheduled prison move with id %s not found.", 2L)
  }

  @Test
  fun cancel_scheduled_move_errors_when_booking_not_associated_with_move() {
    scheduledPrisonMove.offenderBooking = ACTIVE_BOOKING
    whenever(scheduleRepository.findById(scheduledPrisonMove.id))
      .thenReturn(
        Optional.of(scheduledPrisonMove),
      )
    whenever(offenderBookingRepository.findById(99L)).thenReturn(
      Optional.of(
        ACTIVE_BOOKING,
      ),
    )

    assertThatThrownBy {
      service.cancel(
        99L,
        scheduledPrisonMove.id,
        ADMINISTRATIVE_CANCELLATION_REASON,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Booking with id %s not associated with the supplied move id %d.", 99L, scheduledPrisonMove.id)
  }

  @Test
  fun cancel_scheduled_move_errors_when_booking_with_move_not_active() {
    givenScheduledMoveWith(INACTIVE_BOOKING)

    assertThatThrownBy {
      service.cancel(
        INACTIVE_BOOKING.bookingId,
        scheduledPrisonMove.id,
        ADMINISTRATIVE_CANCELLATION_REASON,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage(
        "Offender booking for prison to prison move with id %s is not active.",
        INACTIVE_BOOKING.bookingId,
      )
  }

  @Test
  fun cancel_scheduled_move_errors_when_move_not_in_scheduled_state() {
    givenAnUnscheduledMove()
      .andEventStatusCancelFound()
      .andEventStatusScheduledFound()

    assertThatThrownBy {
      service.cancel(
        ACTIVE_BOOKING.bookingId,
        scheduledPrisonMove.id,
        ADMINISTRATIVE_CANCELLATION_REASON,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Move with id %s is not in a scheduled state.", scheduledPrisonMove.id)
  }

  @Test
  fun cancel_scheduled_move_errors_when_cancellation_reason_not_known() {
    givenScheduledMoveWith(ACTIVE_BOOKING)
      .andEventStatusCancelFound()
      .andEventStatusScheduledFound()

    assertThatThrownBy {
      service.cancel(
        ACTIVE_BOOKING.bookingId,
        scheduledPrisonMove.id,
        "XXXXXX",
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Cancellation reason XXXXXX not found.")
  }

  private fun givenAnActiveBooking(): SchedulePrisonToPrisonMoveSchedulingServiceTest {
    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.of(ACTIVE_BOOKING),
    )

    return this
  }

  private fun andToPrisonNotFound() {
    whenever(agencyLocationRepository.findById(TO_PRISON))
      .thenReturn(Optional.empty())
  }

  private fun andToPrisonNotActive() {
    whenever(agencyLocationRepository.findById(TO_PRISON)).thenReturn(
      Optional.of(
        AgencyLocation.builder()
          .active(true)
          .id(TO_PRISON)
          .description("Prison B description")
          .active(false)
          .build(),
      ),
    )
  }

  private fun andToPrisonIsNotPrison() {
    whenever(agencyLocationRepository.findById(TO_PRISON)).thenReturn(
      Optional.of(
        AgencyLocation.builder()
          .active(true)
          .id(TO_PRISON)
          .description("Prison B description")
          .active(true)
          .type(AgencyLocationType.COURT_TYPE)
          .build(),
      ),
    )
  }

  private fun andValidEscort(): SchedulePrisonToPrisonMoveSchedulingServiceTest {
    whenever(escortAgencyTypeRepository.findById(any()))
      .thenReturn(
        Optional.of<EscortAgencyType>(
          EscortAgencyType(
            PRISON_ESCORT_CUSTODY_SERVICES,
            "Prison Escort Custody Service",
          ),
        ),
      )

    return this
  }

  private fun andEscortNotFound() {
    whenever(escortAgencyTypeRepository.findById(any()))
      .thenReturn(
        Optional.empty<EscortAgencyType>(),
      )
  }

  private fun andValidToPrison(): SchedulePrisonToPrisonMoveSchedulingServiceTest {
    whenever(agencyLocationRepository.findById(TO_PRISON)).thenReturn(
      Optional.of(
        TO_PRISON_AGENCY,
      ),
    )

    return this
  }

  private fun andEventStatusScheduledFound(): SchedulePrisonToPrisonMoveSchedulingServiceTest {
    whenever(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED))
      .thenReturn(Optional.of(SCHEDULED))

    return this
  }

  private fun andEventStatusCancelFound(): SchedulePrisonToPrisonMoveSchedulingServiceTest {
    whenever(eventStatusRepository.findById(EventStatus.CANCELLED)).thenReturn(
      Optional.of<EventStatus?>(CANCELLED),
    )

    return this
  }

  private fun givenAnUnscheduledMove(): SchedulePrisonToPrisonMoveSchedulingServiceTest {
    scheduledPrisonMove.eventStatus = EventStatus("COMP", "Completed")
    scheduledPrisonMove.offenderBooking = ACTIVE_BOOKING

    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.of<OffenderBooking>(ACTIVE_BOOKING),
    )
    whenever(scheduleRepository.findById(scheduledPrisonMove.id))
      .thenReturn(
        Optional.of(scheduledPrisonMove),
      )

    return this
  }

  private fun given(scheduledMove: OffenderIndividualSchedule): SchedulePrisonToPrisonMoveSchedulingServiceTest {
    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.of<OffenderBooking>(ACTIVE_BOOKING),
    )
    whenever(scheduleRepository.findById(scheduledMove.id))
      .thenReturn(Optional.of(scheduledMove))
    whenever(scheduleRepository.findById(scheduledMove.id))
      .thenReturn(Optional.of(scheduledMove))

    return this
  }

  private fun givenScheduledMoveWith(booking: OffenderBooking?): SchedulePrisonToPrisonMoveSchedulingServiceTest {
    scheduledPrisonMove.offenderBooking = booking

    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.of<OffenderBooking>(ACTIVE_BOOKING),
    )
    whenever(scheduleRepository.findById(scheduledPrisonMove.id))
      .thenReturn(
        Optional.of(scheduledPrisonMove),
      )

    return this
  }

  private fun andTransferCancellationReasonFound() {
    whenever(
      transferCancellationReasonRepository.findById(
        any(),
      ),
    ).thenReturn(
      Optional.of<TransferCancellationReason?>(CANCELLATION_REASON),
    )
  }

  companion object {
    private const val OFFENDER_BOOKING_ID = 1L

    private const val FROM_PRISON = "A"

    private val FROM_PRISON_AGENCY: AgencyLocation = AgencyLocation
      .builder().id(FROM_PRISON).description("Prison A description").active(true).build()

    private const val TO_PRISON = "B"

    private val TO_PRISON_AGENCY: AgencyLocation = AgencyLocation
      .builder().id(TO_PRISON).description("Prison A description").active(true).type(AgencyLocationType("INST")).build()

    private const val PRISON_ESCORT_CUSTODY_SERVICES = "PECS"

    private val ACTIVE_BOOKING: OffenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(
        AgencyLocation.builder()
          .active(true)
          .id(FROM_PRISON)
          .description("Prison A description")
          .build(),
      )
      .offender(
        Offender.builder()
          .nomsId("NOMS_ID")
          .build(),
      )
      .build()

    private val INACTIVE_BOOKING: OffenderBooking = OffenderBooking
      .builder()
      .active(false)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(
        AgencyLocation.builder()
          .active(true)
          .id(FROM_PRISON)
          .description("Prison A description")
          .build(),
      )
      .offender(
        Offender.builder()
          .nomsId("NOMS_ID")
          .build(),
      )
      .build()

    private val SCHEDULED = EventStatus("SCH", "Schedule approved.")

    private val CANCELLED = EventStatus("CANC", "Cancelled.")

    private const val APPOINTMENT = "APP"

    private val SCHEDULED_APPOINTMENT: OffenderIndividualSchedule = OffenderIndividualSchedule
      .builder()
      .id(3L)
      .eventType(APPOINTMENT)
      .eventStatus(SCHEDULED)
      .build()

    private const val PRISON_TRANSFER = "TRN"

    private const val ADMINISTRATIVE_CANCELLATION_REASON = "ADMI"

    private val CANCELLATION_REASON =
      TransferCancellationReason(ADMINISTRATIVE_CANCELLATION_REASON, "cancellation reason")
  }
}
