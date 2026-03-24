package uk.gov.justice.hmpps.prison.service

import jakarta.validation.ValidationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.Account
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction
import uk.gov.justice.hmpps.prison.api.model.TransferTransactionDetail
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation.Status.ACTIVE
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository
import uk.gov.justice.hmpps.prison.util.MoneySupport
import uk.gov.justice.hmpps.prison.util.MoneySupport.toMoneyScale
import uk.gov.justice.hmpps.prison.values.AccountCode
import uk.gov.justice.hmpps.prison.values.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import java.util.Optional

@Service
@Transactional(readOnly = true)
class FinanceService(
  private val financeRepository: FinanceRepository,
  private val bookingRepository: BookingRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val offenderTransactionRepository: OffenderTransactionRepository,
  private val accountCodeRepository: AccountCodeRepository,
  private val offenderSubAccountRepository: OffenderSubAccountRepository,
  private val offenderTrustAccountRepository: OffenderTrustAccountRepository,
  private val offenderDamageObligationService: OffenderDamageObligationService,
  private val currency: Currency,
) {

  fun getBalances(bookingId: Long): Account {
    val offenderSummary = bookingRepository.getLatestBookingByBookingId(bookingId)
      .orElseThrow { EntityNotFoundException("Booking not found for id: $bookingId") }

    val damageObligationBalance = offenderDamageObligationService.getDamageObligations(offenderSummary.offenderNo, ACTIVE)
      .map { it.amountToPay.subtract(it.amountPaid) }
      .fold(BigDecimal.ZERO, BigDecimal::add)

    return Optional.ofNullable(financeRepository.getBalances(bookingId, offenderSummary.agencyLocationId))
      .map { it.toBuilder() }
      .map { it.damageObligations(toMoneyScale(damageObligationBalance)).build() }
      .orElse(defaultBalances())
  }

  private fun defaultBalances(): Account {
    val zero = MoneySupport.toMoney("0.00")
    return Account.builder()
      .spends(zero).cash(zero).savings(zero).damageObligations(zero)
      .currency(currency.code).build()
  }

  @Transactional
  fun transferToSavings(prisonId: String, offenderNo: String, transferTransaction: TransferTransaction, clientUniqueId: String? = null): TransferTransactionDetail {
    val optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndActive(offenderNo, true)
    val booking = optionalOffenderBooking.orElseThrow {
      EntityNotFoundException.withMessage(
        "No active offender bookings found for offender number %s",
        offenderNo,
      )
    }

    val subActTypeDr = AccountCode.SPENDS.code
    val subActTypeDrId = accountCodeRepository.findByCaseLoadTypeAndSubAccountType("INST", subActTypeDr).orElseThrow().accountCode
    val subActTypeCr = AccountCode.SAVINGS.code

    validateTransferToSavings(prisonId, offenderNo, transferTransaction, booking, subActTypeDrId, transferTransaction.clientUniqueRef)

    val transactionNumber = offenderTransactionRepository.getNextTransactionId()

    val transferDate = Date()

    financeRepository.insertIntoOffenderTrans(
      prisonId,
      booking.rootOffender.id,
      booking.bookingId,
      "DR",
      subActTypeDr,
      transactionNumber,
      1,
      transferTransaction.amountInPounds,
      transferTransaction.description,
      transferDate,
    )
    financeRepository.insertIntoOffenderTrans(
      prisonId,
      booking.rootOffender.id,
      booking.bookingId,
      "CR",
      subActTypeCr,
      transactionNumber,
      2,
      transferTransaction.amountInPounds,
      transferTransaction.description,
      transferDate,
    )

    val offenderTransaction1 = offenderTransactionRepository.findById(OffenderTransactionId(transactionNumber, 1L))
      .orElseThrow()
    offenderTransaction1.clientUniqueRef = clientUniqueId
    offenderTransaction1.transactionReferenceNumber = transferTransaction.clientTransactionId

    val offenderTransaction2 =
      offenderTransactionRepository.findById(OffenderTransactionId(transactionNumber, 2L)).orElseThrow()
    // client unique ref is unique on the table, so can only mark one of the transactions with the unique ref.
    offenderTransaction2.transactionReferenceNumber = transferTransaction.clientTransactionId

    financeRepository.processGlTransNew(
      prisonId,
      booking.rootOffender.id,
      booking.bookingId,
      subActTypeDr,
      subActTypeCr,
      transactionNumber,
      1,
      transferTransaction.amountInPounds,
      transferTransaction.description,
      transferDate,
      transactionType = "OT",
      moduleName = "OTDSUBAT",
    )

    return TransferTransactionDetail.builder()
      .debitTransaction(Transaction.builder().id("$transactionNumber-1").build())
      .creditTransaction(Transaction.builder().id("$transactionNumber-2").build())
      .transactionId(transactionNumber).build()
  }

  private fun validateTransferToSavings(
    prisonId: String,
    offenderNo: String,
    transferTransaction: TransferTransaction,
    booking: OffenderBooking,
    subActTypeDr: Long,
    clientUniqueRef: String,
  ) {
    if (booking.location.id != prisonId) {
      throw EntityNotFoundException.withMessage(
        "Offender %s found at prison %s instead of %s",
        offenderNo,
        booking.location.id,
        prisonId,
      )
    }

    val optionalOffenderTrustAccount = offenderTrustAccountRepository.findById(
      OffenderTrustAccountId(prisonId, booking.rootOffender.id),
    )

    if (optionalOffenderTrustAccount.isEmpty) {
      throw ValidationException("Offender trust account not found")
    }
    if (optionalOffenderTrustAccount.get().accountClosed) {
      throw ValidationException("Offender trust account closed")
    }

    val optionalOffenderSubAccount = offenderSubAccountRepository.findById(
      OffenderSubAccountId(prisonId, booking.rootOffender.id, subActTypeDr),
    )
    if (optionalOffenderSubAccount.isEmpty) {
      throw ValidationException("Offender sub account not found")
    }
    val balance = optionalOffenderSubAccount.get().balance
    if (balance.compareTo(transferTransaction.amountInPounds) < 0) {
      throw ValidationException(
        String.format(
          "Not enough money in offender sub account balance - %s",
          balance.setScale(2, RoundingMode.HALF_UP),
        ),
      )
    }
    offenderTransactionRepository.findByClientUniqueRef(clientUniqueRef)
      .ifPresent({
        throw DuplicateKeyException(
          String.format("Duplicate post - The unique_client_ref %s has been used before", clientUniqueRef),
        )
      })
  }
}
