package uk.gov.justice.hmpps.prison.service

import jakarta.validation.ValidationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.resource.AddHoldTransaction
import uk.gov.justice.hmpps.prison.api.resource.HoldDetails
import uk.gov.justice.hmpps.prison.api.resource.ReleaseHoldAndCreateTransaction
import uk.gov.justice.hmpps.prison.api.resource.ReleaseHoldTransaction
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.model.PostingType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.TransactionTypeRepository
import uk.gov.justice.hmpps.prison.repository.v1.FinanceV1Repository
import uk.gov.justice.hmpps.prison.util.MoneySupport.penceToPounds
import uk.gov.justice.hmpps.prison.values.AccountCode
import java.math.BigDecimal
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
  private val transactionTypeRepository: TransactionTypeRepository,
  private val financeV1Repository: FinanceV1Repository,
) {
  companion object {
    const val ADD_HOLD_TRANSACTION_TYPE = "HOA"
    const val RELEASE_HOLD_TRANSACTION_TYPE = "HOR"
  }

  fun addHold(
    prisonId: String,
    nomisId: String,
    holdTransaction: AddHoldTransaction,
  ): HoldDetails {
    val subAccountType = AccountCode.SPENDS.code
    val (booking, offenderTrustAccount, offenderSubAccount) = validate(prisonId, nomisId, holdTransaction.clientUniqueReference, subAccountType)

    val addHoldTransactionType = transactionTypeRepository.findById(ADD_HOLD_TRANSACTION_TYPE).get()

    val transactionAmount = penceToPounds(holdTransaction.amount)

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

    val entryDate = Date()
    val now = LocalDateTime.now()
    val transactionId = offenderTransactionRepository.getNextTransactionId()
    val holdNumber = offenderTransactionRepository.getNextTransactionId()

    val transaction = OffenderTransaction(
      id = OffenderTransactionId(transactionId, 1),
      offenderId = booking.rootOffender.id,
      offenderBookingId = booking.bookingId,
      prisonId = prisonId,
      holdNumber = holdNumber,
      holdClearFlag = "N",
      subAccountType = subAccountType,
      transactionType = addHoldTransactionType,
      transactionReferenceNumber = holdTransaction.clientTransactionId,
      clientUniqueRef = holdTransaction.clientUniqueReference,
      entryDate = now.toLocalDate(),
      entryDescription = holdTransaction.description,
      entryAmount = transactionAmount,
      postingType = PostingType.DR,
      modifyDate = now,
    )
    offenderTransactionRepository.save(transaction)

    financeRepository.updateOffenderBalance(
      prisonId,
      booking.rootOffender.id,
      PostingType.DR,
      subAccountType,
      transactionId,
      addHoldTransactionType.type,
      transactionAmount,
      entryDate,
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
      transDate = entryDate,
      transactionType = ADD_HOLD_TRANSACTION_TYPE,
      moduleName = "NOMISAPI",
    )

    offenderSubAccount.holdBalance = offenderSubAccount.holdBalance?.add(transactionAmount) ?: transactionAmount
    offenderTrustAccount.holdBalance = offenderTrustAccount.holdBalance?.add(transactionAmount) ?: transactionAmount

    return HoldDetails(holdNumber)
  }

  fun releaseHold(prisonId: String, nomisId: String, releaseHoldTransaction: ReleaseHoldTransaction, holdNumber: Long): BigDecimal {
    val subAccountType = AccountCode.SPENDS.code
    val (booking, offenderTrustAccount, offenderSubAccount) = validate(prisonId, nomisId, releaseHoldTransaction.clientUniqueReference, subAccountType)
    val holdToReleaseTransaction = offenderTransactionRepository.findAddHoldTransactionForUpdate(booking.rootOffender.id, prisonId, holdNumber)
      .orElseThrow { EntityNotFoundException("Hold transaction not found'") }

    val now = LocalDateTime.now()
    val nowDate = Date()
    val releaseHoldTransactionType = transactionTypeRepository.findById(RELEASE_HOLD_TRANSACTION_TYPE).get()
    val releaseHoldTransactionId = offenderTransactionRepository.getNextTransactionId()

    val releaseTransaction = OffenderTransaction(
      id = OffenderTransactionId(releaseHoldTransactionId, 1),
      offenderId = booking.rootOffender.id,
      offenderBookingId = booking.bookingId,
      prisonId = prisonId,
      holdClearFlag = "Y",
      subAccountType = holdToReleaseTransaction.subAccountType,
      transactionType = releaseHoldTransactionType,
      transactionReferenceNumber = releaseHoldTransaction.clientTransactionId,
      clientUniqueRef = releaseHoldTransaction.clientUniqueReference,
      entryDate = now.toLocalDate(),
      entryDescription = releaseHoldTransaction.description,
      entryAmount = holdToReleaseTransaction.entryAmount,
      postingType = PostingType.CR,
      modifyDate = now,
    )
    offenderTransactionRepository.save(releaseTransaction)

    financeRepository.updateOffenderBalance(
      prisonId,
      booking.rootOffender.id,
      releaseTransaction.postingType,
      holdToReleaseTransaction.subAccountType,
      releaseHoldTransactionId,
      releaseHoldTransactionType.type,
      holdToReleaseTransaction.entryAmount,
      nowDate,
    )

    financeRepository.processGlTransNew(
      prisonId = prisonId, offId = booking.rootOffender.id,
      offBookId = booking.bookingId,
      subActTypeCr = subAccountType,
      subActTypeDr = null,
      transNumber = releaseHoldTransactionId,
      transSeq = 1,
      transAmount = holdToReleaseTransaction.entryAmount,
      transDesc = releaseHoldTransaction.description,
      transDate = nowDate,
      transactionType = RELEASE_HOLD_TRANSACTION_TYPE,
      moduleName = "NOMISAPI",
    )

    holdToReleaseTransaction.holdClearFlag = "Y"

    offenderSubAccount.holdBalance = offenderSubAccount.holdBalance?.minus(holdToReleaseTransaction.entryAmount)
      ?: run {
        throw ValidationException("Offender sub account hold balance not found")
      }

    offenderTrustAccount.holdBalance = offenderTrustAccount.holdBalance?.minus(holdToReleaseTransaction.entryAmount)
      ?: run {
        throw ValidationException("Offender trust account hold balance not found")
      }

    return holdToReleaseTransaction.entryAmount
  }

  fun releaseHoldAndCreateTransaction(prisonId: String, nomisId: String, createTransaction: ReleaseHoldAndCreateTransaction, holdNumber: Long): String {
    if (createTransaction.removeClientUniqueReference == createTransaction.createClientUniqueReference) {
      throw ValidationException("Remove and create client unique references cannot be the same: ${createTransaction.removeClientUniqueReference}")
    }

    val amountInPounds = releaseHold(prisonId, nomisId, createTransaction.toReleaseHold(), holdNumber)

    return financeV1Repository.postTransaction(
      prisonId,
      nomisId,
      createTransaction.type,
      createTransaction.createDescription,
      amountInPounds,
      LocalDate.now(),
      createTransaction.clientTransactionId,
      createTransaction.createClientUniqueReference,
    )
  }

  private fun validate(prisonId: String, nomisId: String, clientUniqueReference: String, subAccountType: String): Triple<OffenderBooking, OffenderTrustAccount, OffenderSubAccount> {
    val booking = offenderBookingRepository.findByOffenderNomsIdAndActive(nomisId, true)
      .orElseThrow { EntityNotFoundException("Offender not found active in prison") }

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

    val subAccountTypeId = accountCodeRepository.findByCaseLoadTypeAndSubAccountType("INST", subAccountType)
      .orElseThrow {
        ValidationException("Account code ${AccountCode.SPENDS.code} not found")
      }.accountCode

    val offenderSubAccount = offenderSubAccountRepository.findById(
      OffenderSubAccountId(prisonId, booking.rootOffender.id, subAccountTypeId),
    ).getOrElse {
      throw ValidationException("Offender sub account not found")
    }

    offenderTransactionRepository.findByClientUniqueRef(clientUniqueReference)
      .ifPresent(
        {
          throw DuplicateKeyException("Duplicate post - The clientUniqueReference $clientUniqueReference has been used before")
        },
      )
    return Triple(booking, offenderTrustAccount.get(), offenderSubAccount)
  }
}
