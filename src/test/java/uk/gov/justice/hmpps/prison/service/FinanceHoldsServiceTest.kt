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
import uk.gov.justice.hmpps.prison.api.resource.AddHoldTransaction
import uk.gov.justice.hmpps.prison.api.resource.ReleaseHoldTransaction
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId
import uk.gov.justice.hmpps.prison.repository.jpa.model.PostingType
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
  val rootOffenderId1 = 345L
  val prisonNumber = "AA2134"
  val rootOffender = Offender().apply {
    id = rootOffenderId1
    rootOffenderId = rootOffenderId1
    nomsId = prisonNumber
  }

  @Nested
  inner class AddHold {
    val transactionId1 = 5454L
    val transactionId2 = 6565L

    val transaction = AddHoldTransaction(
      amount = 1234L,
      clientUniqueReference = "clientRef",
      description = "desc",
      clientTransactionId = "transId",
      clientName = "clientName",
    )

    @BeforeEach
    fun setup() {
      mockFindOffender()
      mockFindOffenderBooking()
      mockFindTrustAccount()
      mockFindAccountCode()
      mockFindAddHoldType()
      mockSubAccount()
      mockClientRefNotExists()
      mockGetNextTransactionIdTwice()
      mockSaveAddHoldTransaction()
    }

    @Nested
    inner class Validation {
      @Test
      fun offenderNotFound() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber)).thenReturn(Optional.empty())

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", prisonNumber, transaction, "clientId")
        }
          .hasMessage("Offender not found")
      }

      @Test
      fun offenderBookingNotFound() {
        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.empty())

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", prisonNumber, transaction, "clientId")
        }
          .hasMessage("Offender not in prison")
      }

      @Test
      fun wrongPrison() {
        val offenderBooking = createOffenderBooking().apply {
          location.id = "WRONG_PRISON"
        }
        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(offenderBooking))

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }
          .hasMessage("Offender AA2134 found at prison WRONG_PRISON instead of LEI")
      }

      @Test
      fun offenderTrustAccountNotFound() {
        whenever(offenderTrustAccountRepository.findById(any())).thenReturn(Optional.empty())

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }
          .hasMessage("Offender trust account not found")
      }

      @Test
      fun offenderTrustAccountClosed() {
        mockFindTrustAccount(closed = true)

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }
          .hasMessage("Offender trust account closed")
      }

      @Test
      fun offenderSubAccountNotFound() {
        whenever(offenderSubAccountRepository.findById(any())).thenReturn(Optional.empty())

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }
          .hasMessage("Offender sub account not found")
      }

      @Test
      fun offenderSubAccountBalanceNotEnough() {
        mockSubAccount(balance = "12")

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientId")
        }.hasMessage("Not enough money in offender sub account balance - 12.00")
      }

      @Test
      fun clientUniqueRefAlreadyUsed() {
        mockClientRefDuplicate()

        assertThatThrownBy {
          financeHoldsService.addHold("LEI", "AA2134", transaction, "clientRef")
        }.hasMessage("Duplicate post - The clientUniqueReference clientRef has been used before")
      }

      @Test
      fun subAccountHoldBalanceDoesNotExistDoesNotFail() {
        whenever(offenderSubAccountRepository.findById(any())).thenReturn(
          Optional.of(
            OffenderSubAccount(
              OffenderSubAccountId("ASI", 1, 2101),
              balance = BigDecimal("12.34"),
            ),
          ),
        )

        financeHoldsService.addHold("LEI", "AA2134", transaction, "clientRef")
      }

      @Test
      fun trustAccountHoldBalanceDoesNotExistDoesNotFail() {
        whenever(offenderTrustAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderTrustAccount()))

        financeHoldsService.addHold("LEI", "AA2134", transaction, "clientRef")
      }
    }

    @Nested
    inner class AddHold {
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

        verify(accountCodeRepository).findByCaseLoadTypeAndSubAccountType("INST", "SPND")

        verify(offenderTransactionRepository, times(2)).getNextTransactionId()

        verify(transactionTypeRepository).findById("HOA")

        verify(offenderSubAccountRepository).findById(OffenderSubAccountId("LEI", rootOffenderId1, 2101L))

        verify(offenderTrustAccountRepository).findById(

          check {
            assertThat(it.prisonId).isEqualTo("LEI")
            assertThat(it.offenderId).isEqualTo(rootOffenderId1)
          },
        )
        verify(offenderTransactionRepository).findByClientUniqueRef("clientRef")

        verify(offenderTransactionRepository).save(
          check {
            assertThat(it.prisonId).isEqualTo("LEI")
            assertThat(it.offenderId).isEqualTo(rootOffenderId1)
            assertThat(it.holdNumber).isEqualTo(transactionId2)
            assertThat(it.subAccountType).isEqualTo("SPND")
            assertThat(it.transactionType.type).isEqualTo("HOA")
            assertThat(it.transactionReferenceNumber).isEqualTo("transId")
            assertThat(it.clientUniqueRef).isEqualTo("clientId", 1)
            assertThat(it.entryDate).isInstanceOf(LocalDate::class.java)
            assertThat(it.entryDescription).isEqualTo("desc")
            assertThat(it.entryAmount).isEqualTo(BigDecimal("12.34"))
            assertThat(it.postingType).isEqualTo(PostingType.DR)
          },
        )

        verify(financeRepository).updateOffenderBalance(
          eq("LEI"),
          eq(rootOffenderId1),
          eq(PostingType.DR),
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

    fun mockFindAddHoldType() {
      whenever(transactionTypeRepository.findById("HOA"))
        .thenReturn(Optional.of(TransactionType("HOA", "Add Hold")))
    }

    private fun mockGetNextTransactionIdTwice() {
      whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(transactionId1)
        .thenReturn(transactionId2)
    }

    private fun mockSaveAddHoldTransaction() {
      whenever(offenderTransactionRepository.save(any<OffenderTransaction>())).thenReturn(
        offenderTransaction(),
      )
    }
  }

  @Nested
  inner class ReleaseHold {
    val prisonNumber = "AA2134"
    val addTransactionId = 5454L
    val releaseTransactionId = 6565L
    val holdNumber = 321L

    val transaction = ReleaseHoldTransaction(
      clientUniqueReference = "clientRef",
      description = "desc",
      clientTransactionId = "transId",
      clientName = "clientName",
    )

    val addHoldOffenderTransaction = OffenderTransaction(
      id = OffenderTransactionId(addTransactionId, 1),
      offenderId = rootOffenderId1,
      prisonId = "ASI",
      holdNumber = holdNumber,
      holdClearFlag = null,
      subAccountType = "SPND",
      transactionType = TransactionType("HOA", "Add Hold"),
      transactionReferenceNumber = null,
      clientUniqueRef = null,
      entryDate = LocalDate.now(),
      entryDescription = null,
      entryAmount = BigDecimal.TEN,
      postingType = PostingType.DR,
      modifyDate = LocalDateTime.now(),
    )
    val releaseHoldOffenderTransaction = OffenderTransaction(
      id = OffenderTransactionId(releaseTransactionId, 1),
      offenderId = 1,
      prisonId = "ASI",
      holdNumber = 3,
      holdClearFlag = "Y",
      subAccountType = "SPND",
      transactionType = TransactionType("HOR", "Remove Hold"),
      transactionReferenceNumber = null,
      clientUniqueRef = null,
      entryDate = LocalDate.now(),
      entryDescription = null,
      entryAmount = BigDecimal.TEN,
      postingType = PostingType.DR,
      modifyDate = LocalDateTime.now(),
    )

    @BeforeEach
    fun setup() {
      mockFindOffender()
      mockFindOffenderBooking()
      mockClientRefNotExists()
      mockFindAddHoldTransaction()
      mockFindAccountCode()
      mockSubAccount()
      mockFindTrustAccount(holdBalance = "6.50")
      mockFindReleaseHoldType()
      mockGetReleaseNextTransactionId()
      mockSaveReleaseTransaction()
    }

    @Nested
    inner class Validation {
      @Test
      fun offenderNotFound() {
        whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber)).thenReturn(Optional.empty())

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", prisonNumber, transaction, "clientId", 1)
        }
          .hasMessage("Offender not found")
      }

      @Test
      fun offenderBookingNotFound() {
        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.empty())

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", prisonNumber, transaction, "clientId", 1)
        }
          .hasMessage("Offender not in prison")
      }

      @Test
      fun wrongPrison() {
        val offenderBooking = createOffenderBooking()
        offenderBooking.location.id = "WRONG_PRISON"
        whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
          .thenReturn(Optional.of(offenderBooking))

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientId", 1)
        }
          .hasMessage("Offender AA2134 found at prison WRONG_PRISON instead of LEI")
      }

      @Test
      fun offenderTrustAccountNotFound() {
        whenever(offenderTrustAccountRepository.findById(any())).thenReturn(Optional.empty())

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientId", holdNumber)
        }
          .hasMessage("Offender trust account not found")
      }

      @Test
      fun offenderTrustAccountClosed() {
        mockFindTrustAccount(closed = true)

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientId", 1)
        }
          .hasMessage("Offender trust account closed")
      }

      @Test
      fun offenderSubAccountNotFound() {
        whenever(offenderSubAccountRepository.findById(any())).thenReturn(Optional.empty())

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientId", 1)
        }
          .hasMessage("Offender sub account not found")
      }

      @Test
      fun clientUniqueRefAlreadyUsed() {
        mockClientRefDuplicate()

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientRef", 1)
        }.hasMessage("Duplicate post - The clientUniqueReference clientRef has been used before")
      }

      @Test
      fun subAccountHoldBalanceDoesNotExist() {
        whenever(offenderSubAccountRepository.findById(any())).thenReturn(
          Optional.of(
            OffenderSubAccount(
              OffenderSubAccountId("ASI", 1, 2101),
              balance = BigDecimal("12.34"),
            ),
          ),
        )

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientRef", 1)
        }.hasMessage("Offender sub account hold balance not found")
      }

      @Test
      fun trustAccountHoldBalanceDoesNotExist() {
        whenever(offenderTrustAccountRepository.findById(any()))
          .thenReturn(Optional.of(offenderTrustAccount()))

        assertThatThrownBy {
          financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientRef", 1)
        }.hasMessage("Offender trust account hold balance not found")
      }
    }

    @Nested
    inner class ReleaseHold {
      @Test
      fun happyPath() {
        financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientUniqueId", 1)
        verify(offenderTransactionRepository).save(
          check {
            assertThat(it.id.transactionId).isEqualTo(releaseTransactionId)
            assertThat(it.clientUniqueRef).isEqualTo("clientUniqueId")
            assertThat(it.transactionReferenceNumber).isEqualTo("transId")
            assertThat(it.holdNumber).isNull()
            assertThat(it.holdClearFlag).isEqualTo("Y")
          },
        )
      }

      @Test
      fun setClientUniqueRef() {
        financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientUniqueId", holdNumber)

        verify(offenderTransactionRepository).save(
          check {
            assertThat(it.clientUniqueRef).isEqualTo("clientUniqueId")
            assertThat(it.transactionReferenceNumber).isEqualTo("transId")
          },
        )
      }

      @Test
      fun verifyCalls() {
        financeHoldsService.releaseHold("LEI", "AA2134", transaction, "clientId", holdNumber)

        verify(offenderRepository).findRootOffenderByNomsId("AA2134")

        verify(offenderBookingRepository).findByOffenderNomsIdAndActive("AA2134", true)

        verify(offenderTransactionRepository).findAddHoldTransactionForUpdate(rootOffenderId1, "LEI", holdNumber)

        verify(accountCodeRepository).findByCaseLoadTypeAndSubAccountType("INST", "SPND")
        verify(offenderSubAccountRepository).findById(OffenderSubAccountId("LEI", rootOffenderId1, 2101L))
        verify(offenderTrustAccountRepository).findById(

          check {
            assertThat(it.prisonId).isEqualTo("LEI")
            assertThat(it.offenderId).isEqualTo(rootOffenderId1)
          },
        )
        verify(transactionTypeRepository).findById("HOR")

        verify(offenderTransactionRepository).getNextTransactionId()

        verify(offenderTransactionRepository).findByClientUniqueRef("clientRef")

        verify(offenderTransactionRepository).save(
          check {
            assertThat(it.prisonId).isEqualTo("LEI")
            assertThat(it.offenderId).isEqualTo(rootOffenderId1)
            assertThat(it.holdNumber).isNull()
            assertThat(it.subAccountType).isEqualTo("SPND")
            assertThat(it.transactionType.type).isEqualTo("HOR")
            assertThat(it.transactionReferenceNumber).isEqualTo("transId")
            assertThat(it.clientUniqueRef).isEqualTo("clientId")
            assertThat(it.entryDate).isInstanceOf(LocalDate::class.java)
            assertThat(it.entryDescription).isEqualTo("desc")
            assertThat(it.entryAmount).isEqualTo(BigDecimal.TEN)
            assertThat(it.postingType).isEqualTo(PostingType.CR)
          },
        )

        verify(financeRepository).updateOffenderBalance(
          eq("LEI"),
          eq(rootOffenderId1),
          eq(PostingType.CR),
          eq("SPND"),
          eq(releaseTransactionId),
          eq("HOR"),
          eq(BigDecimal.TEN),
          any(),
        )
      }
    }
    private fun mockGetReleaseNextTransactionId() {
      whenever(offenderTransactionRepository.getNextTransactionId())
        .thenReturn(releaseTransactionId)
    }

    private fun mockFindAddHoldTransaction() {
      whenever(offenderTransactionRepository.findAddHoldTransactionForUpdate(any(), anyString(), any()))
        .thenReturn(Optional.of(addHoldOffenderTransaction))
    }

    fun mockFindReleaseHoldType() {
      whenever(transactionTypeRepository.findById("HOR"))
        .thenReturn(Optional.of(TransactionType("HOR", "Remove Hold")))
    }

    private fun mockSaveReleaseTransaction() {
      whenever(offenderTransactionRepository.save(any<OffenderTransaction>())).thenReturn(
        releaseHoldOffenderTransaction,
      )
    }
  }

  private fun offenderTrustAccount(accountClosed: Boolean = false) = OffenderTrustAccount(
    id = OffenderTrustAccountId("ASI", 1L),
    accountClosed = accountClosed,
  )

  private fun offenderSubAccount(balance: String = "12.34") = OffenderSubAccount(
    OffenderSubAccountId("ASI", 1, 2101),
    balance = BigDecimal(balance),
    holdBalance = BigDecimal("3.50"),
  )

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
    postingType = PostingType.CR,
    modifyDate = LocalDateTime.now(),
  )

  private fun mockFindOffender() {
    whenever(offenderRepository.findRootOffenderByNomsId(prisonNumber)).thenReturn(Optional.of(rootOffender))
  }

  private fun createOffenderBooking(prisonId: String = "LEI") = OffenderBooking.builder()
    .bookingId(1L)
    .rootOffender(Offender.builder().id(rootOffenderId1).build())
    .location(AgencyLocation.builder().id(prisonId).build())
    .build()

  private fun mockFindOffenderBooking(prison: String = "LEI") {
    whenever(offenderBookingRepository.findByOffenderNomsIdAndActive(anyString(), eq(true)))
      .thenReturn(Optional.of(createOffenderBooking(prison)))
  }

  fun mockFindAccountCode() {
    whenever(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND")))
      .thenReturn(Optional.of(AccountCode.builder().accountCode(2101L).build()))
  }

  fun mockFindTrustAccount(closed: Boolean = false, holdBalance: String? = null) {
    whenever(offenderTrustAccountRepository.findById(any()))
      .thenReturn(
        Optional.of(
          offenderTrustAccount(closed).apply {
            this.holdBalance = holdBalance?.let { BigDecimal(it) }
          },
        ),
      )
  }

  private fun mockSubAccount(balance: String = "12.34", holdBalance: String = "3.50") {
    whenever(offenderSubAccountRepository.findById(any()))
      .thenReturn(
        Optional.of(
          offenderSubAccount(balance).apply {
            this.holdBalance = BigDecimal(holdBalance)
          },
        ),
      )
  }

  private fun mockClientRefNotExists() {
    whenever(offenderTransactionRepository.findByClientUniqueRef(anyString()))
      .thenReturn(Optional.empty())
  }

  private fun mockClientRefDuplicate() {
    whenever(offenderTransactionRepository.findByClientUniqueRef(anyString()))
      .thenReturn(Optional.of(offenderTransaction()))
  }
}
