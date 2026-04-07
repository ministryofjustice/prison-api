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
import uk.gov.justice.hmpps.prison.util.ResourceUtils
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
    clientUniqueId: String,
  ): HoldDetails {
    val (rootOffenderId, booking, offenderTrustAccount) = validate(prisonId, nomisId)

    val subAccountType = AccountCode.SPENDS.code
    val subAccountTypeId = accountCodeRepository.findByCaseLoadTypeAndSubAccountType("INST", subAccountType)
      .orElseThrow {
        ValidationException("Account code ${AccountCode.SPENDS.code} not found")
      }.accountCode

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

    offenderTransactionRepository.findByClientUniqueRef(clientUniqueId)
      .ifPresent(
        {
          throw DuplicateKeyException("Duplicate post - The clientUniqueReference $clientUniqueId has been used before")
        },
      )
    val entryDate = Date()
    val now = LocalDateTime.now()
    val transactionId = offenderTransactionRepository.getNextTransactionId()
    val holdNumber = offenderTransactionRepository.getNextTransactionId()

    val transaction = OffenderTransaction(
      id = OffenderTransactionId(transactionId, 1),
      offenderId = rootOffenderId,
      offenderBookingId = booking.bookingId,
      prisonId = prisonId,
      holdNumber = holdNumber,
      holdClearFlag = "N",
      subAccountType = subAccountType,
      transactionType = addHoldTransactionType,
      transactionReferenceNumber = holdTransaction.clientTransactionId,
      clientUniqueRef = clientUniqueId,
      entryDate = now.toLocalDate(),
      entryDescription = holdTransaction.description,
      entryAmount = transactionAmount,
      postingType = PostingType.DR,
      modifyDate = now,
    )
    offenderTransactionRepository.save(transaction)

    financeRepository.updateOffenderBalance(
      prisonId,
      rootOffenderId,
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
      // TODO Check - there is anywhere to add  transactionReferenceNumber = holdTransaction.clientTransactionId,
    )

    offenderSubAccount.holdBalance = offenderSubAccount.holdBalance?.add(transactionAmount) ?: transactionAmount
    offenderTrustAccount.holdBalance = offenderTrustAccount.holdBalance?.add(transactionAmount) ?: transactionAmount

    return HoldDetails(holdNumber)
  }

  fun releaseHold(prisonId: String, nomisId: String, releaseHoldTransaction: ReleaseHoldTransaction, clientUniqueId: String, holdNumber: Long): BigDecimal {
    val (rootOffenderId, booking, offenderTrustAccount) = validate(prisonId, nomisId)

    val holdToReleaseTransaction = offenderTransactionRepository.findAddHoldTransactionForUpdate(
      rootOffenderId,
      prisonId,
      holdNumber,
    )
      .orElseThrow { EntityNotFoundException("Hold transaction not found'") }

    val subAccountTypeId = accountCodeRepository.findByCaseLoadTypeAndSubAccountType("INST", holdToReleaseTransaction.subAccountType)
      .orElseThrow {
        ValidationException("Account code ${AccountCode.SPENDS.code} not found")
      }.accountCode
    val offenderSubAccount = offenderSubAccountRepository.findById(
      OffenderSubAccountId(prisonId, booking.rootOffender.id, subAccountTypeId),
    ).getOrElse {
      throw ValidationException("Offender sub account not found")
    }

    offenderTransactionRepository.findByClientUniqueRef(clientUniqueId)
      .ifPresent(
        {
          throw DuplicateKeyException("Duplicate post - The clientUniqueReference $clientUniqueId has been used before")
        },
      )

    val releaseHoldTransactionId = offenderTransactionRepository.getNextTransactionId()
    val now = LocalDateTime.now()
    val nowDate = Date()
    val releaseHoldTransactionType = transactionTypeRepository.findById(RELEASE_HOLD_TRANSACTION_TYPE).get()

    val releaseTransaction = OffenderTransaction(
      id = OffenderTransactionId(releaseHoldTransactionId, 1),
      offenderId = rootOffenderId,
      offenderBookingId = booking.bookingId,
      prisonId = prisonId,
      holdClearFlag = "Y",
      subAccountType = holdToReleaseTransaction.subAccountType,
      transactionType = releaseHoldTransactionType,
      transactionReferenceNumber = releaseHoldTransaction.clientTransactionId,
      clientUniqueRef = clientUniqueId,
      entryDate = now.toLocalDate(),
      entryDescription = releaseHoldTransaction.description,
      entryAmount = holdToReleaseTransaction.entryAmount,
      postingType = PostingType.CR,
      modifyDate = now,
    )
    offenderTransactionRepository.save(releaseTransaction)

    financeRepository.updateOffenderBalance(
      prisonId,
      rootOffenderId,
      releaseTransaction.postingType,
      holdToReleaseTransaction.subAccountType,
      releaseHoldTransactionId,
      releaseHoldTransactionType.type,
      holdToReleaseTransaction.entryAmount,
      nowDate,
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

    val removeClientUniqueId = ResourceUtils.getUniqueClientId(createTransaction.clientName, createTransaction.removeClientUniqueReference)
    val amountInPounds = releaseHold(prisonId, nomisId, createTransaction.toReleaseHold(), removeClientUniqueId, holdNumber)

    val createClientUniqueId = ResourceUtils.getUniqueClientId(createTransaction.clientName, createTransaction.createClientUniqueReference)
    return financeV1Repository.postTransaction(
      prisonId,
      nomisId,
      createTransaction.type,
      createTransaction.createDescription,
      amountInPounds,
      LocalDate.now(),
      createTransaction.clientTransactionId,
      createClientUniqueId,
    )
  }

  private fun validate(prisonId: String, nomisId: String): Triple<Long, OffenderBooking, OffenderTrustAccount> {
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

    return Triple(booking.rootOffender.id, booking, offenderTrustAccount.get())
  }
}
