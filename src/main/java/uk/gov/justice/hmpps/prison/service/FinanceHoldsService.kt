package uk.gov.justice.hmpps.prison.service

import jakarta.validation.ValidationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.resource.HoldDetails
import uk.gov.justice.hmpps.prison.api.resource.HoldTransaction
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.TransactionTypeRepository
import uk.gov.justice.hmpps.prison.util.MoneySupport.penceToPounds
import uk.gov.justice.hmpps.prison.values.AccountCode
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import kotlin.jvm.optionals.getOrElse

@Service
@Transactional
class FinanceHoldsService(
  private val financeRepository: FinanceRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val offenderTransactionRepository: OffenderTransactionRepository,
  private val accountCodeRepository: AccountCodeRepository,
  private val offenderSubAccountRepository: OffenderSubAccountRepository,
  private val offenderTrustAccountRepository: OffenderTrustAccountRepository,
  private val offenderRepository: OffenderRepository,
  private val transactionTypeRepository: TransactionTypeRepository,
) {
  companion object {
    const val ADD_HOLD_TRANSACTION_TYPE = "HOA"
    const val RELEASE_HOLD_TRANSACTION_TYPE = "HOR"
  }

  fun addHold(
    prisonId: String,
    nomisId: String,
    holdTransaction: HoldTransaction,
    clientUniqueId: String,
  ): HoldDetails {
    val rootOffender = offenderRepository.findRootOffenderByNomsId(nomisId)
      .orElseThrow { EntityNotFoundException("Offender not found") }

    val booking = offenderBookingRepository.findByOffenderNomsIdAndActive(nomisId, true)
      .orElseThrow { EntityNotFoundException("Offender not in prison") }

    if (booking.location.id != prisonId) {
      throw EntityNotFoundException.withMessage(
        "Offender %s found at prison %s instead of %s",
        nomisId,
        booking.location.id,
        prisonId,
      )
    }

    val offenderTrustAccount = offenderTrustAccountRepository.findById(
      OffenderTrustAccountId(prisonId, booking.rootOffender.id),
    )

    if (offenderTrustAccount.isEmpty) {
      throw ValidationException("Offender trust account not found")
    }
    if (offenderTrustAccount.get().accountClosed) {
      throw ValidationException("Offender trust account closed")
    }

    val subAccountType = AccountCode.SPENDS.code
    val subAccountTypeId = accountCodeRepository.findByCaseLoadTypeAndSubAccountType("INST", subAccountType)
      .orElseThrow {
        ValidationException("Account code ${AccountCode.SPENDS.code} not found")
      }.accountCode

    val transactionId = offenderTransactionRepository.getNextTransactionId()
    val holdNumber = offenderTransactionRepository.getNextTransactionId()
    val addHoldTransactionType = transactionTypeRepository.findById(ADD_HOLD_TRANSACTION_TYPE).get()

    val transactionAmount = penceToPounds(holdTransaction.amount)

    val offenderSubAccount = offenderSubAccountRepository.findById(
      OffenderSubAccountId(prisonId, booking.rootOffender.id, subAccountTypeId),
    ).getOrElse {
      throw ValidationException("Offender sub account not found")
    }

    val balance = offenderSubAccount.balance
    if (balance < transactionAmount) {
      throw ValidationException(
        "Not enough money in offender sub account balance - ${
          balance.setScale(
            2,
            RoundingMode.HALF_UP,
          )
        }",
      )
    }

    offenderTransactionRepository.findByClientUniqueRef(holdTransaction.clientUniqueReference)
      .ifPresent(
        {
          throw DuplicateKeyException("Duplicate post - The clientUniqueReference ${holdTransaction.clientUniqueReference} has been used before")
        },
      )

    val transaction = OffenderTransaction(
      id = OffenderTransactionId(transactionId, 1),
      offenderId = rootOffender.id,
      offenderBookingId = booking.bookingId,
      prisonId = prisonId,
      holdNumber = holdNumber,
      holdClearFlag = "N",
      subAccountType = subAccountType,
      transactionType = addHoldTransactionType,
      transactionReferenceNumber = holdTransaction.clientTransactionId,
      clientUniqueRef = clientUniqueId,
      entryDate = LocalDate.now(),
      entryDescription = holdTransaction.description,
      entryAmount = transactionAmount,
      postingType = "DR",
      modifyDate = LocalDateTime.now(),
    )
    offenderTransactionRepository.save(transaction)

    financeRepository.updateOffenderBalance(
      prisonId,
      rootOffender.id,
      "DR",
      subAccountType,
      transactionId,
      addHoldTransactionType.type,
      transactionAmount,
      Date(),
    )

    financeRepository.processGlTransNew(
      prisonId = prisonId, offId = booking.rootOffender.id,
      offBookId = booking.bookingId,
      subActTypeCr = null,
      subActTypeDr = subAccountType,
      transNumber = transactionId,
      transSeq = 1,
      transAmount = transactionAmount,
      transDesc = holdTransaction.description,
      transDate = Date(),
      transactionType = ADD_HOLD_TRANSACTION_TYPE,
      moduleName = "NOMISAPI",
      // TODO Check - there is anywhere to add  transactionReferenceNumber = holdTransaction.clientTransactionId,
    )

    offenderSubAccount.holdBalance = offenderSubAccount.holdBalance?.add(transactionAmount) ?: transactionAmount
    offenderTrustAccount.get().holdBalance = offenderTrustAccount.get().holdBalance?.add(transactionAmount) ?: transactionAmount

    return HoldDetails(holdNumber)
  }
}
