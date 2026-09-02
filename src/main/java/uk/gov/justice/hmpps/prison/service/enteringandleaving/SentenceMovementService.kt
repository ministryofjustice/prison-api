package uk.gov.justice.hmpps.prison.service.enteringandleaving

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository

@Service
@Transactional
class SentenceMovementService(
  private val offenderSentenceAdjustmentRepository: OffenderSentenceAdjustmentRepository,
  private val offenderKeyDateAdjustmentRepository: OffenderKeyDateAdjustmentRepository,
  private val offenderSentenceRepository: OffenderSentenceRepository,
) {
  fun deactivateSentences(bookingId: Long, deactivateSentences: Boolean) {
    deactivateSentenceAdjustments(bookingId)
    deactivateKeyDateAdjustments(bookingId)
    if (deactivateSentences) {
      deactivateSentences(bookingId)
    }
  }

  private fun deactivateSentenceAdjustments(bookingId: Long) = offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingIdAndActive(bookingId, true)
    .forEach { it.isActive = false }

  private fun deactivateKeyDateAdjustments(bookingId: Long) = offenderKeyDateAdjustmentRepository.findAllByOffenderBooking_BookingIdAndActive(bookingId, true)
    .forEach { it.isActive = false }

  private fun deactivateSentences(bookingId: Long) = offenderSentenceRepository.findAllByOffenderBooking_BookingIdAndStatus(bookingId, "A")
    .forEach { it.status = "I" }
}
