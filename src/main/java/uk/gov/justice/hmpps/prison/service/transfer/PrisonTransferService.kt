package uk.gov.justice.hmpps.prison.service.transfer

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn
import uk.gov.justice.hmpps.prison.exception.CustomErrorCodes
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramEndReason
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.ConflictingRequestException
import uk.gov.justice.hmpps.prison.service.CourtHearingsService
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Transactional
@Service
class PrisonTransferService(
  private val externalMovementService: ExternalMovementTransferService,
  private val bedAssignmentTransferService: BedAssignmentTransferService,
  private val trustAccountService: TrustAccountService,
  private val iepTransferService: IEPTransferService,
  private val caseNoteTransferService: CaseNoteTransferService,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository,
  private val agencyLocationRepository: AgencyLocationRepository,
  private val activityTransferService: ActivityTransferService,
  private val courtHearingsService: CourtHearingsService,
  private val teamWorkflowNotificationService: TeamWorkflowNotificationService,
  private val transformer: OffenderTransformer,
) {
  fun transferFromPrison(offenderNo: String, request: RequestToTransferIn): InmateDetail {
    val booking = getLatestOffenderBooking(offenderNo).flatMap { it.assertIsBeingTransferred() }.getOrThrow()
    val transferMovement = booking.getLatestMovement().flatMap { it.assertIsActiveTransfer() }.getOrThrow()
    val cellLocation =
      getCellLocation(request.cellLocation, transferMovement.toAgency).flatMap { it.assertHasSpaceInCell() }
        .getOrThrow()

    with(booking) {
      inOutStatus = MovementDirection.IN.name
      isActive = true
      bookingStatus = "O"
      assignedLivingUnit = cellLocation
      bookingEndDate = null
      location = transferMovement.toAgency
      livingUnitMv = null
      externalMovementService.updateMovementsForTransfer(
        request = request, booking = booking, lastMovement = transferMovement
      ).also { movement ->
        statusReason = "${movement.movementType.code}-${movement.movementReason.code}"
        bedAssignmentTransferService.createBedHistory(
          booking = this, cellLocation = cellLocation, receiveTime = movement.movementTime, reasonCode = MovementType.ADM.code
        )
        trustAccountService.createTrustAccount(
          booking = this, fromAgency = transferMovement.fromAgency, movementIn = movement
        )
        iepTransferService.resetLevelForPrison(booking = this, transferMovement = movement)
        caseNoteTransferService.createGenerateAdmissionNote(booking = this, transferMovement = movement)
      }
    }

    return transformer.transform(booking)
  }

  fun transferViaCourt(offenderNo: String, request: RequestForCourtTransferIn): InmateDetail {

    val booking = getLatestOffenderBooking(offenderNo).flatMap { it.assertIsOut() }.getOrThrow()
    val transferMovement = booking.getLatestMovement().flatMap { it.assertIsActiveCourtTransfer() }.getOrThrow()

    return if (request.agencyId.equals(transferMovement.fromAgency.id)) transferViaCourtFromSamePrison(
      booking, request, transferMovement
    ) else transferViaCourtFromDifferentPrison(booking, request, transferMovement)
  }

  fun transferViaCourtFromDifferentPrison(
    booking: OffenderBooking,
    request: RequestForCourtTransferIn,
    toCourtMovement: ExternalMovement
  ): InmateDetail {
    val reception =
      getCellLocation(null, request.agencyId).getOrThrow()
    val toAgency = getAgencyLocation(request.agencyId).getOrThrow()

    with(booking) {
      inOutStatus = MovementDirection.IN.name
      livingUnitMv = null
      assignedLivingUnit = reception
      location = toAgency
      teamWorkflowNotificationService.sendTransferViaCourtNotification(booking) {
        externalMovementService.updateMovementsForCourtTransferToDifferentPrison(
          movementDateTime = request.dateTime,
          booking = booking,
          lastMovement = toCourtMovement,
          toAgency = toAgency,
          commentText = request.commentText
        ).also { createdMovement ->
          statusReason = "${createdMovement.movementType.code}-${createdMovement.movementReason.code}"
          bedAssignmentTransferService.createBedHistory(
            booking = this, cellLocation = reception, receiveTime = createdMovement.movementTime
          )
          activityTransferService.endActivitiesAndWaitlist(
            booking,
            toCourtMovement.fromAgency,
            createdMovement.movementDate,
            OffenderProgramEndReason.TRF.code
          )
          trustAccountService.createTrustAccount(
            booking = this, fromAgency = toCourtMovement.fromAgency, movementIn = createdMovement
          )
          iepTransferService.resetLevelForPrison(booking = this, transferMovement = createdMovement)
          caseNoteTransferService.createGenerateAdmissionNote(booking = this, transferMovement = createdMovement)
        }
      }
    }
    return transformer.transform(booking)
  }

  fun transferViaCourtFromSamePrison(
    booking: OffenderBooking,
    request: RequestForCourtTransferIn,
    toCourtMovement: ExternalMovement
  ): InmateDetail {
    val courtEvent: CourtEvent? = toCourtMovement.eventId?.let {
      courtHearingsService.completeScheduledChildHearingEvent(booking.bookingId, it).orElse(null)
    }
    with(booking) {
      inOutStatus = MovementDirection.IN.name
      statusReason = MovementType.CRT.code + "-" + (request.movementReasonCode ?: toCourtMovement.movementReason.code)
      livingUnitMv = null
      externalMovementService.updateMovementsForCourtTransferToSamePrison(
        movementReasonCode = request.movementReasonCode,
        movementDateTime = request.dateTime,
        booking = booking,
        lastMovement = toCourtMovement,
        courtEvent = courtEvent,
        commentText = request.commentText
      )
    }

    return transformer.transform(booking)
  }

  fun transferViaTemporaryAbsence(): InmateDetail {
    TODO()
  }

  private fun getLatestOffenderBooking(offenderNo: String): Result<OffenderBooking> {
    return offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1).map { success(it) }
      .orElse(failure(EntityNotFoundException.withMessage("No bookings found for prisoner number $offenderNo")))
  }

  private fun getCellLocation(cellLocation: String?, prison: AgencyLocation): Result<AgencyInternalLocation> {
    return getCellLocation(cellLocation, prison.id)
  }

  private fun getCellLocation(cellLocation: String?, prisonId: String): Result<AgencyInternalLocation> {
    val internalLocationCode = cellLocation ?: "$prisonId-RECP"
    return agencyInternalLocationRepository.findOneByDescriptionAndAgencyId(
      internalLocationCode, prisonId
    ).map { success(it) }
      .orElse(failure(EntityNotFoundException.withMessage("$internalLocationCode cell location not found")))
  }

  private fun getAgencyLocation(prisonId: String): Result<AgencyLocation> {
    return agencyLocationRepository.findById(
      prisonId
    ).map { success(it) }
      .orElse(failure(EntityNotFoundException.withMessage("$prisonId agency not found")))
  }
}

private fun OffenderBooking.isBeingTransferred() = this.inOutStatus == "TRN"
private fun OffenderBooking.isOut() = this.inOutStatus == "OUT"
private fun OffenderBooking.assertIsBeingTransferred(): Result<OffenderBooking> = if (!isBeingTransferred()) {
  failure(BadRequestException("Prisoner is not currently being transferred"))
} else {
  success(this)
}

private fun OffenderBooking.assertIsOut(): Result<OffenderBooking> = if (!isOut()) {
  failure(BadRequestException("Prisoner is not currently out"))
} else {
  success(this)
}

private fun OffenderBooking.getLatestMovement(): Result<ExternalMovement> {
  return this.lastMovement.map { success(it) }
    .orElse(failure(EntityNotFoundException.withMessage("Not movements found to transfer in")))
}

private fun ExternalMovement.assertIsActiveTransfer(): Result<ExternalMovement> {
  if (!this.isTransfer()) {
    return failure(BadRequestException("Latest movement not a transfer"))
  }
  if (!this.isActive) {
    return failure(BadRequestException("Transfer not active"))
  }

  return success(this)
}

private fun ExternalMovement.assertIsActiveCourtTransfer(): Result<ExternalMovement> {
  if (!this.isCourtTransfer()) {
    return failure(BadRequestException("Latest movement not a court transfer"))
  }
  if (!this.isActive) {
    return failure(BadRequestException("Transfer not active"))
  }

  return success(this)
}

private fun ExternalMovement.isTransfer() = this.movementType.code == MovementType.TRN.code
private fun ExternalMovement.isCourtTransfer() = this.movementType.code == MovementType.CRT.code
private fun AgencyInternalLocation.assertHasSpaceInCell(): Result<AgencyInternalLocation> = if (this.hasSpace(true)) {
  success(this)
} else {
  failure(
    ConflictingRequestException.withMessage(
      "The cell ${this.description} does not have any available capacity",
      CustomErrorCodes.NO_CELL_CAPACITY
    )
  )
}

private inline fun <T> Result<T>.flatMap(transform: (value: T) -> Result<T>): Result<T> {
  return when {
    isSuccess -> transform(getOrThrow())
    else -> this
  }
}
