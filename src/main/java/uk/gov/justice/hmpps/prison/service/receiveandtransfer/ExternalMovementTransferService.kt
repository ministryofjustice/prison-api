package uk.gov.justice.hmpps.prison.service.receiveandtransfer

import jakarta.persistence.EntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDateTime
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Service
class ExternalMovementTransferService(
  private val movementReasonRepository: ReferenceCodeRepository<MovementReason>,
  private val externalMovementRepository: ExternalMovementRepository,
  private val movementTypeRepository: ReferenceCodeRepository<MovementType>,
  private val entityManager: EntityManager,
) {
  fun updateMovementsForTransfer(
    request: RequestToTransferIn,
    booking: OffenderBooking,
    lastMovement: ExternalMovement
  ): ExternalMovement {
    val movementReason = getMovementReasonForPrisonTransfer().getOrThrow()
    val receiveDateTime = getReceiveDateTime(request.receiveTime, booking).getOrThrow()
    val movementType = getAdmissionMovementType().getOrThrow()

    return booking.addExternalMovementIn(
      movementDateTime = receiveDateTime,
      fromLocation = lastMovement.fromAgency,
      prison = lastMovement.toAgency,
      commentText = request.commentText,
      movementReason = movementReason,
      movementType = movementType,
    )
  }

  fun updateMovementsForCourtTransferToDifferentPrison(
    movementDateTime: LocalDateTime?,
    booking: OffenderBooking,
    lastMovement: ExternalMovement,
    toAgency: AgencyLocation,
    commentText: String?
  ): ExternalMovement {
    val movementReason = getMovementReason(MovementReason.TRANSFER_VIA_COURT.code).getOrThrow()
    val receiveDateTime = getReceiveDateTime(movementDateTime, booking).getOrThrow()
    val movementType = getAdmissionMovementType().getOrThrow()

    return booking.addExternalMovementIn(
      movementDateTime = receiveDateTime,
      fromLocation = lastMovement.fromAgency,
      prison = toAgency,
      commentText = commentText,
      movementReason = movementReason,
      movementType = movementType,
    )
  }

  fun updateMovementsForCourtTransferToSamePrison(
    movementReasonCode: String?,
    movementDateTime: LocalDateTime?,
    booking: OffenderBooking,
    lastMovement: ExternalMovement,
    courtEvent: CourtEvent?,
    commentText: String?
  ): ExternalMovement {
    val movementReason = getMovementReason(movementReasonCode ?: lastMovement.movementReason.code).getOrThrow()
    val receiveDateTime = getReceiveDateTime(movementDateTime, booking).getOrThrow()
    val movementType = getCourtMovementType().getOrThrow() // not an admission as only returning from court

    return booking.addExternalMovementIn(
      movementDateTime = receiveDateTime,
      eventId = courtEvent?.id,
      parentEventId = courtEvent?.parentCourtEventId,
      fromLocation = lastMovement.toAgency,
      prison = lastMovement.fromAgency,
      commentText = commentText,
      movementReason = movementReason,
      movementType = movementType,
    )
  }

  fun updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
    movementReasonCode: String?,
    movementDateTime: LocalDateTime?,
    booking: OffenderBooking,
    lastMovement: ExternalMovement,
    scheduleEvent: OffenderIndividualSchedule?,
    commentText: String?
  ): ExternalMovement {
    val movementReason = getMovementReason(movementReasonCode ?: lastMovement.movementReason.code).getOrThrow()
    val receiveDateTime = getReceiveDateTime(movementDateTime, booking).getOrThrow()
    val movementType = getTAPMovementType().getOrThrow()

    return booking.addExternalMovementIn(
      movementDateTime = receiveDateTime,
      eventId = scheduleEvent?.id,
      parentEventId = scheduleEvent?.parentEventId,
      fromLocation = lastMovement.toAgency,
      prison = lastMovement.fromAgency,
      escortCode = lastMovement.escortCode,
      commentText = commentText,
      fromCity = lastMovement.toCity,
      movementReason = movementReason,
      movementType = movementType,
      fromAddressId = lastMovement.toAddressId,
    )
  }

  fun updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
    movementDateTime: LocalDateTime?,
    booking: OffenderBooking,
    lastMovement: ExternalMovement,
    toAgency: AgencyLocation,
    commentText: String?
  ): ExternalMovement {
    val movementReason = getMovementReason(MovementReason.TRANSFER_VIA_TAP.code).getOrThrow()
    val receiveDateTime = getReceiveDateTime(movementDateTime, booking).getOrThrow()
    val movementType = getAdmissionMovementType().getOrThrow()

    return booking.addExternalMovementIn(
      movementDateTime = receiveDateTime,
      fromLocation = lastMovement.fromAgency,
      prison = toAgency,
      commentText = commentText,
      movementReason = movementReason,
      movementType = movementType,
    )
  }

  fun updateMovementsForNewOrRecalledBooking(
    booking: OffenderBooking,
    movementReasonCode: String,
    fromLocation: AgencyLocation,
    prison: AgencyLocation,
    receiveDateTime: LocalDateTime,
    commentText: String
  ): ExternalMovement {
    val movementReason = getMovementReason(movementReasonCode).getOrThrow()
    val movementType = getAdmissionMovementType().getOrThrow()

    return booking.addExternalMovementIn(
      movementDateTime = receiveDateTime,
      fromLocation = fromLocation,
      prison = prison,
      commentText = commentText,
      movementReason = movementReason,
      movementType = movementType,
    )
  }

  private fun getAdmissionMovementType(): Result<MovementType> =
    movementTypeRepository.findByIdOrNull(MovementType.ADM)?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("No ${MovementType.ADM} movement type found")
    )

  private fun getCourtMovementType(): Result<MovementType> =
    movementTypeRepository.findByIdOrNull(MovementType.CRT)?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("No ${MovementType.CRT} movement type found")
    )

  private fun getTAPMovementType(): Result<MovementType> =
    movementTypeRepository.findByIdOrNull(MovementType.TAP)?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("No ${MovementType.TAP} movement type found")
    )

  private fun getMovementReasonForPrisonTransfer(): Result<MovementReason> {
    return movementReasonRepository.findByIdOrNull(MovementReason.pk("INT"))
      ?.let { success(it) }
      ?: return failure(EntityNotFoundException.withMessage("No movement reason INT found"))
  }

  private fun getMovementReason(movementReasonCode: String): Result<MovementReason> =
    movementReasonRepository.findByIdOrNull(MovementReason.pk(movementReasonCode))
      ?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("No movement reason $movementReasonCode found"))

  fun getReceiveDateTime(movementTime: LocalDateTime?, booking: OffenderBooking?): Result<LocalDateTime> {
    val now = LocalDateTime.now()
    return movementTime?.let {
      return if (movementTime.isAfter(now)) {
        failure(BadRequestException("Transfer cannot be done in the future"))
      } else if (booking?.hasMovementsAfter(movementTime) == true) {
        failure(BadRequestException("Movement cannot be before the previous active movement"))
      } else {
        success(movementTime)
      }
    } ?: success(now)
  }

  private fun OffenderBooking.hasMovementsAfter(movementTime: LocalDateTime) =
    externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(this.bookingId, true).any {
      movementTime.isBefore(it.movementTime)
    }

  private fun OffenderBooking.addExternalMovementIn(
    movementDateTime: LocalDateTime,
    eventId: Long? = null,
    parentEventId: Long? = null,
    fromLocation: AgencyLocation? = null,
    prison: AgencyLocation,
    escortCode: String? = null,
    commentText: String? = null,
    fromCity: City? = null,
    movementReason: MovementReason,
    movementType: MovementType,
    fromAddressId: Long? = null,
  ): ExternalMovement = this.setPreviousMovementsToInactive().also { entityManager.flush() }.let {
    this.addExternalMovement(
      ExternalMovement(
        /* offenderBooking = */ this,
        /* movementSequence = */ null,
        /* movementDate = */ movementDateTime.toLocalDate(),
        /* reportingDate = */ null,
        /* movementTime = */ movementDateTime,
        /* eventId = */ eventId,
        /* parentEventId = */ parentEventId,
        /* arrestAgencyLocation = */ null,
        /* fromAgency = */ fromLocation,
        /* toAgency = */ prison, // the passed in agency in the request is just for validation
        /* active = */ true,
        /* escortText = */ null,
        /* escortCode = */ escortCode,
        /* commentText = */ commentText,
        /* toCity = */ null,
        /* fromCity = */ fromCity,
        /* movementReason = */ movementReason,
        /* movementDirection = */ MovementDirection.IN,
        /* movementType = */ movementType,
        /* toAddressId = */ null,
        /* fromAddressId = */ fromAddressId,
      )
    )
  }
}
