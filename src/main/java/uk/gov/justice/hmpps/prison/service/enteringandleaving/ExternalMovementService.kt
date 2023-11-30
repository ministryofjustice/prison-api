package uk.gov.justice.hmpps.prison.service.enteringandleaving

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
class ExternalMovementService(
  private val movementReasonRepository: ReferenceCodeRepository<MovementReason>,
  private val externalMovementRepository: ExternalMovementRepository,
  private val movementTypeRepository: ReferenceCodeRepository<MovementType>,
  private val entityManager: EntityManager,
) {
  fun updateMovementsForTransferIn(
    request: RequestToTransferIn,
    booking: OffenderBooking,
    lastMovement: ExternalMovement,
  ): ExternalMovement {
    val movementReason = getMovementReasonForPrisonTransfer().getOrThrow()
    val receiveDateTime = getMovementDateTime(request.receiveTime, booking).getOrThrow()
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
    commentText: String?,
  ): ExternalMovement {
    val movementReason = getMovementReason(MovementReason.TRANSFER_VIA_COURT.code).getOrThrow()
    val receiveDateTime = getMovementDateTime(movementDateTime, booking).getOrThrow()
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
    commentText: String?,
  ): ExternalMovement {
    val movementReason = getMovementReason(movementReasonCode ?: lastMovement.movementReason.code).getOrThrow()
    val receiveDateTime = getMovementDateTime(movementDateTime, booking).getOrThrow()
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
    commentText: String?,
  ): ExternalMovement {
    val movementReason = getMovementReason(movementReasonCode ?: lastMovement.movementReason.code).getOrThrow()
    val receiveDateTime = getMovementDateTime(movementDateTime, booking).getOrThrow()
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
    commentText: String?,
  ): ExternalMovement {
    val movementReason = getMovementReason(MovementReason.TRANSFER_VIA_TAP.code).getOrThrow()
    val receiveDateTime = getMovementDateTime(movementDateTime, booking).getOrThrow()
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
    commentText: String,
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

  fun updateMovementsForRelease(
    releaseTime: LocalDateTime? = null,
    movementReasonCode: String,
    booking: OffenderBooking,
    toAgency: AgencyLocation,
    commentText: String,
  ): ExternalMovement {
    val receiveDateTime = getMovementDateTime(releaseTime, booking).getOrThrow()
    val movementType = getReleaseMovementType().getOrThrow()
    val movementReason = getMovementReason(movementReasonCode).getOrThrow()

    return booking.addExternalMovementOut(
      movementDateTime = receiveDateTime,
      prison = booking.location,
      toLocation = toAgency,
      commentText = commentText,
      movementReason = movementReason,
      movementType = movementType,
    )
  }

  private fun getAdmissionMovementType(): Result<MovementType> =
    movementTypeRepository.findByIdOrNull(MovementType.ADM)?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("No ${MovementType.ADM} movement type found"),
    )

  private fun getCourtMovementType(): Result<MovementType> =
    movementTypeRepository.findByIdOrNull(MovementType.CRT)?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("No ${MovementType.CRT} movement type found"),
    )

  private fun getTAPMovementType(): Result<MovementType> =
    movementTypeRepository.findByIdOrNull(MovementType.TAP)?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("No ${MovementType.TAP} movement type found"),
    )

  private fun getReleaseMovementType(): Result<MovementType> =
    movementTypeRepository.findByIdOrNull(MovementType.REL)?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("No ${MovementType.REL} movement type found"),
    )

  private fun getMovementReasonForPrisonTransfer(): Result<MovementReason> {
    return movementReasonRepository.findByIdOrNull(MovementReason.pk("INT"))
      ?.let { success(it) }
      ?: return failure(EntityNotFoundException.withMessage("No movement reason INT found"))
  }

  fun getMovementReason(movementReasonCode: String): Result<MovementReason> =
    movementReasonRepository.findByIdOrNull(MovementReason.pk(movementReasonCode))
      ?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("No movement reason $movementReasonCode found"))

  fun getMovementDateTime(movementTime: LocalDateTime?, booking: OffenderBooking?): Result<LocalDateTime> {
    val now = LocalDateTime.now()
    return movementTime?.let {
      return if (movementTime.isAfter(now)) {
        failure(BadRequestException("Movement cannot be done in the future"))
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
      ExternalMovement.builder()
        .movementDate(movementDateTime.toLocalDate())
        .movementTime(movementDateTime)
        .eventId(eventId)
        .parentEventId(parentEventId)
        .fromAgency(fromLocation)
        .toAgency(prison)
        .escortCode(escortCode)
        .commentText(commentText)
        .fromCity(fromCity)
        .movementReason(movementReason)
        .movementDirection(MovementDirection.IN)
        .movementType(movementType)
        .fromAddressId(fromAddressId)
        .build(),
    )
  }

  private fun OffenderBooking.addExternalMovementOut(
    movementDateTime: LocalDateTime,
    eventId: Long? = null,
    parentEventId: Long? = null,
    prison: AgencyLocation,
    toLocation: AgencyLocation,
    escortCode: String? = null,
    commentText: String? = null,
    movementReason: MovementReason,
    movementType: MovementType,
  ): ExternalMovement = this.setPreviousMovementsToInactive().also { entityManager.flush() }.let {
    this.addExternalMovement(
      ExternalMovement.builder()
        .movementDate(movementDateTime.toLocalDate())
        .movementTime(movementDateTime)
        .eventId(eventId)
        .parentEventId(parentEventId)
        .fromAgency(prison)
        .toAgency(toLocation)
        .escortCode(escortCode)
        .commentText(commentText)
        .movementReason(movementReason)
        .movementDirection(MovementDirection.OUT)
        .movementType(movementType)
        .reportingDate(movementDateTime.toLocalDate())
        .build(),
    )
  }
}
