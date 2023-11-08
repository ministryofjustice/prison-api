package uk.gov.justice.hmpps.prison.service.enteringandleaving

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository

@Service
@Transactional
class SentenceMovementService(
  private val offenderSentenceAdjustmentRepository: OffenderSentenceAdjustmentRepository,
  private val offenderKeyDateAdjustmentRepository: OffenderKeyDateAdjustmentRepository,
) {
  fun deactivateSentences(bookingId: Long) {
    deactivateSentenceAdjustments(bookingId)
    deactivateKeyDateAdjustments(bookingId)
  }

  private fun deactivateSentenceAdjustments(bookingId: Long) =
    offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingIdAndActive(bookingId, true)
      .forEach { it.isActive = false }

  private fun deactivateKeyDateAdjustments(bookingId: Long) =
    offenderKeyDateAdjustmentRepository.findAllByOffenderBooking_BookingIdAndActive(bookingId, true)
      .forEach { it.isActive = false }
}
