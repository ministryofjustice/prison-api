package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.RequestToRecall
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner
import uk.gov.justice.hmpps.prison.api.resource.UpdatePrisonerDetails
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq.BookingAndSeq
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findOffenderByNomsIdOrNull
import uk.gov.justice.hmpps.prison.service.enteringandleaving.BookingIntoPrisonService
import java.time.LocalDateTime

@Service
class SmokeTestHelperService(
  private val bookingService: BookingService,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val prisonerReleaseAndTransferService: PrisonerReleaseAndTransferService,
  private val bookingIntoPrisonService: BookingIntoPrisonService,
  private val offenderRepository: OffenderRepository,
) {
  @Transactional
  fun updatePrisonerDetails(offenderNo: String, prisonerDetails: UpdatePrisonerDetails) {
    offenderRepository.findOffenderByNomsIdOrNull(offenderNo)?.apply {
      firstName = prisonerDetails.firstName.uppercase()
      lastName = prisonerDetails.lastName.uppercase()
      offenderRepository.save(this)
    } ?: throw EntityNotFoundException.withMessage("Offender $offenderNo not found")
  }

  @Transactional
  fun imprisonmentDataSetup(offenderNo: String) {
    val latestOffenderBooking = bookingService.getOffenderIdentifiers(offenderNo, "SMOKE_TEST")
    val bookingAndSeq = getBookingAndSeqOrThrow(offenderNo, latestOffenderBooking)
    val booking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, bookingAndSeq.bookingSeq)
      .orElseThrow(
        EntityNotFoundException.withMessage("No booking found for offender $offenderNo and seq ${bookingAndSeq.bookingSeq}"),
      )
    val currentImprisonmentStatus = booking.activeImprisonmentStatus.orElseThrow {
      BadRequestException("Unable to find active imprisonment status for offender number $offenderNo")
    }
    booking.setImprisonmentStatus(currentImprisonmentStatus.toBuilder().build(), LocalDateTime.now())
  }

  private fun getBookingAndSeqOrThrow(offenderNo: String, latestOffenderBooking: OffenderBookingIdSeq): BookingAndSeq =
    latestOffenderBooking.bookingAndSeq
      .orElseThrow { EntityNotFoundException.withMessage("No booking found for offender $offenderNo") }

  @Transactional
  fun releasePrisoner(offenderNo: String) {
    val requestToReleasePrisoner = RequestToReleasePrisoner.builder()
      .commentText("Prisoner was released as part of smoke test")
      .movementReasonCode("CR")
      .build()
    prisonerReleaseAndTransferService.releasePrisoner(offenderNo, requestToReleasePrisoner, null)
  }

  @Transactional
  fun recallPrisoner(offenderNo: String) {
    val requestToRecall = RequestToRecall.builder()
      .prisonId("LEI")
      .movementReasonCode("24")
      .imprisonmentStatus("CUR_ORA")
      .build()
    bookingIntoPrisonService.recallPrisoner(offenderNo, requestToRecall)
  }
}
