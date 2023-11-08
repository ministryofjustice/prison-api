package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import java.time.LocalDateTime
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Service
@Transactional
class ReleasePrisonerService(
  private val offenderBookingRepository: OffenderBookingRepository,
  private val agencyLocationRepository: AgencyLocationRepository,
  private val externalMovementService: ExternalMovementService,
  private val transformer: OffenderTransformer,
  private val caseNoteMovementService: CaseNoteMovementService,
  private val bedAssignmentMovementService: BedAssignmentMovementService,
  private val sentenceMovementService: SentenceMovementService,
  private val paymentsMovementService: PaymentsMovementService,
  private val activityMovementService: ActivityMovementService,
) {

  fun releasePrisoner(prisonerIdentifier: String, request: RequestToReleasePrisoner): InmateDetail {
    val booking = getOffenderBooking(prisonerIdentifier).flatMap { it.isActiveIn() }.getOrThrow()
    val toLocation = getAgencyLocation(request.toLocationCode).getOrThrow()

    val movement = externalMovementService.updateMovementsForRelease(request.releaseTime, request.movementReasonCode, booking, toLocation, request.commentText)

    caseNoteMovementService.createReleaseNote(booking, movement)
    bedAssignmentMovementService.endBedHistory(booking.bookingId, movement.movementTime)
    sentenceMovementService.deactivateSentences(booking.bookingId)
    paymentsMovementService.endPaymentRules(booking.bookingId, movement.movementDate)
    activityMovementService.endActivitiesAndWaitlist(booking, movement.fromAgency, movement.movementDate, request.movementReasonCode)

    return booking.release(toLocation, movement.movementTime, movement.movementReason.code)
      .let { transformer.transform(it) }
  }

  private fun getOffenderBooking(offenderNo: String): Result<OffenderBooking> =
    offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1)
      .map { success(it) }
      .orElse(failure(EntityNotFoundException.withMessage("No bookings found for prisoner number $offenderNo")))

  private fun OffenderBooking.isActiveIn(): Result<OffenderBooking> =
    if (!isActive) {
      failure(BadRequestException("Booking $bookingId is not active"))
    } else if (!isIn) {
      failure(BadRequestException("Booking $bookingId is not IN"))
    } else {
      success(this)
    }

  private fun OffenderBooking.release(toLocation: AgencyLocation, releaseTime: LocalDateTime, reasonCode: String) =
    apply {
      inOutStatus = "OUT"
      isActive = false
      bookingStatus = "C"
      livingUnitMv = null
      assignedLivingUnit = null
      location = toLocation
      bookingEndDate = releaseTime
      statusReason = "REL-$reasonCode"
      commStatus = null
    }

  private fun getAgencyLocation(prisonId: String): Result<AgencyLocation> =
    agencyLocationRepository.findByIdOrNull(prisonId)
      ?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("No $prisonId agency found"))
}
