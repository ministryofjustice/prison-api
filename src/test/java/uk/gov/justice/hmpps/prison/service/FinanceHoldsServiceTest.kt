package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.resource.HoldTransaction
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.TransactionTypeRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

internal class FinanceHoldsServiceTest {
  private val financeRepository: FinanceRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val offenderTransactionRepository: OffenderTransactionRepository = mock()
  private val accountCodeRepository: AccountCodeRepository = mock()
  private val offenderSubAccountRepository: OffenderSubAccountRepository = mock()
  private val offenderTrustAccountRepository: OffenderTrustAccountRepository = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val transactionTypeRepository: TransactionTypeRepository = mock()

  private val financeHoldsService: FinanceHoldsService = FinanceHoldsService(
    financeRepository,
    offenderBookingRepository,
    offenderTransactionRepository,
    accountCodeRepository,
    offenderSubAccountRepository,
    offenderTrustAccountRepository,
    offenderRepository,
    transactionTypeRepository,
  )

  @Nested
  inner class AddHold {
    val prisonNumber = "AA2134"
    val offenderId = 123L
    val rootOffenderId = 345L
    val transactionId1 = 5454L
    val transactionId2 = 6565L

    val transaction = HoldTransaction(
      amount = 1234L,
      clientUniqueRef = "clientRef",
      description = "desc",
      clientTransactionId = "transId",
      accountCode = "spends",
    )

    val offender = Offender().apply {
      id = offenderId
      rootOffenderId = rootOffenderId
      nomsId = prisonNumber
    }

    @Nested
    inner class Validation {
      @Test
      fun offenderNotFound() {
        assertThatThrownBy {
          financeHoldsService.addHold("LEI", prisonNumber, transaction, "clientId")
        }
          .hasMessage("Offender not found")
      }

      @Test
      fun offenderBookingNotFound() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber)).thenReturn(Optional.of(offender))
        assertThatThrownBy {
          financeHoldsService.addHold("LEI", prisonNumber, transaction, "clientId")
        }
          .hasMessage("Offender not in prison")
      }

      @Test
      fun wrongPrison() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber)).thenReturn(Optional.of(offender))

        val offenderBooking = createOffenderBooking()
        offenderBooking.location.id = "WRONG_PRISON"
        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(offenderBooking))

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }
          .hasMessage("Offender AA2134 found at prison WRONG_PRISON instead of LEI")
      }

      @Test
      fun offenderTrustAccountNotFound() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber))
          .thenReturn(Optional.of(offender))

        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(createOffenderBooking()))

        whenever(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND")))
          .thenReturn(Optional.of(AccountCode.builder().accountCode(2101L).build()))

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }
          .hasMessage("Offender trust account not found")
      }

      @Test
      fun offenderTrustAccountClosed() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber)).thenReturn(Optional.of(offender))

        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(createOffenderBooking()))

        whenever(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND")))
          .thenReturn(Optional.of(AccountCode.builder().accountCode(2101L).build()))

        whenever(offenderTrustAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderTrustAccount(true)))

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }
          .hasMessage("Offender trust account closed")
      }

      @Test
      fun offenderSubAccountNotFound() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber))
          .thenReturn(Optional.of(offender))

        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(createOffenderBooking()))

        whenever(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND")))
          .thenReturn(Optional.of(AccountCode.builder().accountCode(2101L).build()))

        whenever(offenderTrustAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderTrustAccount()))

        whenever(transactionTypeRepository.findById("HOA"))
          .thenReturn(Optional.of(TransactionType("HOA", "Add Hold")))

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }
          .hasMessage("Offender sub account not found")
      }

      @Test
      fun offenderSubAccountBalanceNotEnough() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber))
          .thenReturn(Optional.of(offender))

        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(createOffenderBooking()))

        whenever(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND")))
          .thenReturn(Optional.of(AccountCode.builder().accountCode(2101L).build()))

        whenever(offenderTrustAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderTrustAccount()))
        whenever(offenderSubAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderSubAccount(balance = "12")))

        whenever(transactionTypeRepository.findById("HOA"))
          .thenReturn(Optional.of(TransactionType("HOA", "Add Hold")))

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }.hasMessage("Not enough money in offender sub account balance - 12.00")
      }

      @Test
      fun clientUniqueRefAlreadyUsed() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber))
          .thenReturn(Optional.of(offender))

        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(createOffenderBooking()))

        whenever(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND")))
          .thenReturn(Optional.of(AccountCode.builder().accountCode(2101L).build()))

        whenever(offenderTrustAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderTrustAccount()))
        whenever(offenderSubAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderSubAccount()))

        whenever(offenderTransactionRepository.findByClientUniqueRef(anyString()))
          .thenReturn(Optional.of(offenderTransaction()))

        whenever(transactionTypeRepository.findById("HOA"))
          .thenReturn(Optional.of(TransactionType("HOA", "Add Hold")))

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientRef")
        }.hasMessage("Duplicate post - The unique_client_ref clientRef has been used before")
      }
    }

    @Nested
    inner class AddHold {

      @BeforeEach
      fun setup() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber))
          .thenReturn(Optional.of(offender))

        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(createOffenderBooking()))

        whenever(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND")))
          .thenReturn(Optional.of(AccountCode.builder().accountCode(2101L).build()))

        whenever(offenderTrustAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderTrustAccount()))
        whenever(offenderSubAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderSubAccount()))

        whenever(transactionTypeRepository.findById("HOA"))
          .thenReturn(Optional.of(TransactionType("HOA", "Add Hold")))

        whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(transactionId1).thenReturn(transactionId2)

        whenever(offenderTransactionRepository.save(any<OffenderTransaction>())).thenReturn(
          offenderTransaction(),
        )
      }

      @Test
      fun happyPath() {
        val hold = financeHoldsService.addHold("LEI", "AA2134", transaction, "clientUniqueId")
        assertThat(hold.holdNumber).isEqualTo(transactionId2)
        verify(offenderTransactionRepository).save(
          check {
            assertThat(it.clientUniqueRef).isEqualTo("clientUniqueId")
            assertThat(it.transactionReferenceNumber).isEqualTo("transId")
          },
        )
      }

      @Test
      fun setClientUniqueRef() {
        val hold = financeHoldsService.addHold("LEI", "AA2134", transaction, "clientUniqueId")

        assertThat(hold.holdNumber).isEqualTo(transactionId2)
        verify(offenderTransactionRepository).save(
          check {
            assertThat(it.clientUniqueRef).isEqualTo("clientUniqueId")
            assertThat(it.transactionReferenceNumber).isEqualTo("transId")
          },
        )
      }

      @Test
      fun verifyCalls() {
        financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")

        verify(offenderRepository).findRootOffenderByNomsId("AA2134")

        verify(offenderBookingRepository).findByOffenderNomsIdAndActive("AA2134", true)

        verify(offenderTrustAccountRepository).findById(

          check {
            assertThat(it.prisonId).isEqualTo("LEI")
            assertThat(it.offenderId).isEqualTo(rootOffenderId)
          },
        )

        verify(accountCodeRepository).findByCaseLoadTypeAndSubAccountType("INST", "SPND")
        verify(offenderTransactionRepository, times(2)).getNextTransactionId()

        verify(transactionTypeRepository).findById("HOA")

        verify(offenderSubAccountRepository).findById(OffenderSubAccountId("LEI", rootOffenderId, 2101L))

        verify(offenderTransactionRepository).findByClientUniqueRef("clientRef")

        verify(offenderTransactionRepository).save(
          check {
            assertThat(it.prisonId).isEqualTo("LEI")
            assertThat(it.offenderId).isEqualTo(offenderId)
            assertThat(it.holdNumber).isEqualTo(transactionId2)
            assertThat(it.subAccountType).isEqualTo("SPND")
            assertThat(it.transactionType.type).isEqualTo("HOA")
            assertThat(it.transactionReferenceNumber).isEqualTo("transId")
            assertThat(it.clientUniqueRef).isEqualTo("clientId")
            assertThat(it.entryDate).isInstanceOf(LocalDate::class.java)
            assertThat(it.entryDescription).isEqualTo("desc")
            assertThat(it.entryAmount).isEqualTo(BigDecimal("12.34"))
            assertThat(it.postingType).isEqualTo("DR")
          },
        )

        verify(financeRepository).updateOffenderBalance(
          eq("LEI"),
          eq(offenderId),
          eq("DR"),
          eq("SPND"),
          eq(transactionId1),
          eq("HOA"),
          eq(BigDecimal("12.34")),
          any(),
        )

        verify(financeRepository).processGlTransNew(
          eq("LEI"),
          eq(345L),
          eq(1L),
          eq("SPND"),
          eq(null),
          eq(transactionId1),
          eq(1L),
          eq(BigDecimal("12.34")),
          eq("desc"),
          any(),
          eq("HOA"),
          eq("NOMISAPI"),
        )
      }
    }

    private fun createOffenderBooking() = OffenderBooking.builder()
      .bookingId(1L)
      .rootOffender(Offender.builder().id(rootOffenderId).build())
      .location(AgencyLocation.builder().id("LEI").build())
      .build()
  }

  private fun offenderTransaction(
    id: OffenderTransactionId = OffenderTransactionId(1, 1),
  ) = OffenderTransaction(
    id = id,
    offenderId = 1,
    prisonId = "BMI",
    holdNumber = null,
    holdClearFlag = null,
    subAccountType = "REG",
    transactionType = TransactionType("CANT", "Canteen"),
    transactionReferenceNumber = null,
    clientUniqueRef = null,
    entryDate = LocalDate.now(),
    entryDescription = null,
    entryAmount = BigDecimal.TEN,
    postingType = "CR",
    modifyDate = LocalDateTime.now(),
  )

  private fun offenderTrustAccount(accountClosed: Boolean = false) = OffenderTrustAccount(
    id = OffenderTrustAccountId("ASI", 1L),
    accountClosed = accountClosed,
  )
  fun offenderTransaction2(
    id: OffenderTransactionId = OffenderTransactionId(1, 1),
  ) = OffenderTransaction(
    id = id,
    offenderId = 1,
    offenderBookingId = 1,
    prisonId = "BMI",
    holdNumber = null,
    holdClearFlag = null,
    subAccountType = "REG",
    transactionType = TransactionType("CANT", "Canteen"),
    transactionReferenceNumber = null,
    clientUniqueRef = null,
    entryDate = LocalDate.now(),
    entryDescription = null,
    entryAmount = BigDecimal.TEN,
    modifyDate = LocalDateTime.now(),
    postingType = "",
  )
}
