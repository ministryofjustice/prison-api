package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNoPayPeriodRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPayStatusRepository
import java.time.LocalDate

@Service
@Transactional
class PaymentsMovementService(
  private val payStatusRepository: OffenderPayStatusRepository,
  private val noPayPeriodRepository: OffenderNoPayPeriodRepository,
) {

  fun endPaymentRules(bookingId: Long) {
    endPayStatus(bookingId)
    endNoPayPeriods(bookingId)
  }

  private fun endPayStatus(bookingId: Long) {
    payStatusRepository.findAllByBookingId(bookingId)
      .filter { it.endDate == null || it.endDate.isAfter(LocalDate.now()) }
      .forEach { it.endDate = LocalDate.now() }
  }

  private fun endNoPayPeriods(bookingId: Long) {
    noPayPeriodRepository.findAllByBookingId(bookingId)
      .filter { it.endDate == null || it.endDate.isAfter(LocalDate.now()) }
      .forEach { it.endDate = LocalDate.now() }
  }
}
