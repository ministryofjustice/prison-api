package uk.gov.justice.hmpps.prison.service.receiveandtransfer

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
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
import javax.persistence.EntityManager
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

    return with(booking) {
      setPreviousMovementsToInactive().also { entityManager.flush() }
      addExternalMovement(
        ExternalMovement(
          /* offenderBooking = */ booking,
          /* movementSequence = */ null,
          /* movementDate = */ receiveDateTime.toLocalDate(),
          /* reportingDate = */ null,
          /* movementTime = */ receiveDateTime,
          /* eventId = */ null,
          /* parentEventId = */ null,
          /* arrestAgencyLocation = */ null,
          /* fromAgency = */ lastMovement.fromAgency,
          /* toAgency = */ lastMovement.toAgency,
          /* active = */ true,
          /* escortText = */ null,
          /* commentText = */ request.commentText,
          /* toCity = */ null,
          /* fromCity = */ null,
          /* movementReason = */ movementReason,
          /* movementDirection = */ MovementDirection.IN,
          /* movementType = */ movementType,
          /* toAddressId = */ null,
          /* fromAddressId = */ null,
        )
      )
    }
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

    return with(booking) {
      setPreviousMovementsToInactive().also { entityManager.flush() }
      addExternalMovement(
        ExternalMovement(
          /* offenderBooking = */ booking,
          /* movementSequence = */ null,
          /* movementDate = */ receiveDateTime.toLocalDate(),
          /* reportingDate = */ null,
          /* movementTime = */ receiveDateTime,
          /* eventId = */ null,
          /* parentEventId = */ null,
          /* arrestAgencyLocation = */ null,
          /* fromAgency = */ lastMovement.fromAgency,
          /* toAgency = */ toAgency, // the passed in agency in the request is just for validation
          /* active = */ true,
          /* escortText = */ null,
          /* commentText = */ commentText,
          /* toCity = */ null,
          /* fromCity = */ null,
          /* movementReason = */ movementReason,
          /* movementDirection = */ MovementDirection.IN,
          /* movementType = */ movementType,
          /* toAddressId = */ null,
          /* fromAddressId = */ null,
        )
      )
    }
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

    return with(booking) {
      setPreviousMovementsToInactive().also { entityManager.flush() }
      addExternalMovement(
        ExternalMovement(
          /* offenderBooking = */ booking,
          /* movementSequence = */ null,
          /* movementDate = */ receiveDateTime.toLocalDate(),
          /* reportingDate = */ null,
          /* movementTime = */ receiveDateTime,
          /* eventId = */ courtEvent?.id,
          /* parentEventId = */ courtEvent?.parentCourtEventId,
          /* arrestAgencyLocation = */ null,
          /* fromAgency = */ lastMovement.toAgency,
          /* toAgency = */ lastMovement.fromAgency,
          /* active = */ true,
          /* escortText = */ null,
          /* commentText = */ commentText,
          /* toCity = */ null,
          /* fromCity = */ null,
          /* movementReason = */ movementReason,
          /* movementDirection = */ MovementDirection.IN,
          /* movementType = */ movementType,
          /* toAddressId = */ null,
          /* fromAddressId = */ null,
        )
      )
    }
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

    return with(booking) {
      setPreviousMovementsToInactive().also { entityManager.flush() }
      addExternalMovement(
        ExternalMovement(
          /* offenderBooking = */ booking,
          /* movementSequence = */ null,
          /* movementDate = */ receiveDateTime.toLocalDate(),
          /* reportingDate = */ null,
          /* movementTime = */ receiveDateTime,
          /* eventId = */ scheduleEvent?.id,
          /* parentEventId = */ scheduleEvent?.parentEventId,
          /* arrestAgencyLocation = */ null,
          /* fromAgency = */ lastMovement.toAgency,
          /* toAgency = */ lastMovement.fromAgency,
          /* active = */ true,
          /* escortText = */ null,
          /* commentText = */ commentText,
          /* toCity = */ null,
          /* fromCity = */ lastMovement.toCity,
          /* movementReason = */ movementReason,
          /* movementDirection = */ MovementDirection.IN,
          /* movementType = */ movementType,
          /* toAddressId = */ null,
          /* fromAddressId = */ lastMovement.toAddressId,
        )
      )
    }
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

    return with(booking) {
      setPreviousMovementsToInactive().also { entityManager.flush() }
      addExternalMovement(
        ExternalMovement(
          /* offenderBooking = */ booking,
          /* movementSequence = */ null,
          /* movementDate = */ receiveDateTime.toLocalDate(),
          /* reportingDate = */ null,
          /* movementTime = */ receiveDateTime,
          /* eventId = */ null,
          /* parentEventId = */ null,
          /* arrestAgencyLocation = */ null,
          /* fromAgency = */ lastMovement.fromAgency,
          /* toAgency = */ toAgency, // the passed in agency in the request is just for validation
          /* active = */ true,
          /* escortText = */ null,
          /* commentText = */ commentText,
          /* toCity = */ null,
          /* fromCity = */ null,
          /* movementReason = */ movementReason,
          /* movementDirection = */ MovementDirection.IN,
          /* movementType = */ movementType,
          /* toAddressId = */ null,
          /* fromAddressId = */ null,
        )
      )
    }
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

    return with(booking) {
      setPreviousMovementsToInactive().also { entityManager.flush() }
      addExternalMovement(
        ExternalMovement(
          /* offenderBooking = */ booking,
          /* movementSequence = */ null,
          /* movementDate = */ receiveDateTime.toLocalDate(),
          /* reportingDate = */ null,
          /* movementTime = */ receiveDateTime,
          /* eventId = */ null,
          /* parentEventId = */ null,
          /* arrestAgencyLocation = */ null,
          /* fromAgency = */ fromLocation,
          /* toAgency = */ prison, // the passed in agency in the request is just for validation
          /* active = */ true,
          /* escortText = */ null,
          /* commentText = */ commentText,
          /* toCity = */ null,
          /* fromCity = */ null,
          /* movementReason = */ movementReason,
          /* movementDirection = */ MovementDirection.IN,
          /* movementType = */ movementType,
          /* toAddressId = */ null,
          /* fromAddressId = */ null,
        )
      )
    }
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
}
