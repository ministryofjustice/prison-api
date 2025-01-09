package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.Account
import uk.gov.justice.hmpps.prison.api.model.OffenderDamageObligationModel
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository
import uk.gov.justice.hmpps.prison.util.MoneySupport
import uk.gov.justice.hmpps.prison.values.Currency
import java.math.BigDecimal
import java.util.Optional

internal class FinanceServiceTest {
  private val financeRepository: FinanceRepository = mock()
  private val bookingRepository: BookingRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val offenderTransactionRepository: OffenderTransactionRepository = mock()
  private val accountCodeRepository: AccountCodeRepository = mock()
  private val offenderSubAccountRepository: OffenderSubAccountRepository = mock()
  private val offenderTrustAccountRepository: OffenderTrustAccountRepository = mock()
  private val offenderDamageObligationService: OffenderDamageObligationService = mock()

  private val financeService: FinanceService = FinanceService(
    financeRepository,
    bookingRepository,
    offenderBookingRepository,
    offenderTransactionRepository,
    accountCodeRepository,
    offenderSubAccountRepository,
    offenderTrustAccountRepository,
    offenderDamageObligationService,
    Currency.builder().code("GBP").build(),
  )

  @Test
  fun testTransfer_offenderNotFound() {
    val transaction = createTransferTransaction()
    Assertions.assertThatThrownBy {
      financeService.transferToSavings(
        "LEI",
        "AA2134",
        transaction,
        "1234",
      )
    }
      .hasMessage("No active offender bookings found for offender number AA2134")
  }

  @Test
  fun testTransfer_wrongPrison() {
    val transaction = createTransferTransaction()

    val offenderBooking = createOffenderBooking()
    offenderBooking.location.id = "WRONG_PRISON"
    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(offenderBooking),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    Assertions.assertThatThrownBy {
      financeService.transferToSavings(
        "LEI",
        "AA2134",
        transaction,
        "1234",
      )
    }
      .hasMessage("Offender AA2134 found at prison WRONG_PRISON instead of LEI")
  }

  @Test
  fun testTransfer_offenderTrustAccountNotFound() {
    val transaction = createTransferTransaction()

    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(createOffenderBooking()),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    Assertions.assertThatThrownBy {
      financeService.transferToSavings(
        "LEI",
        "AA2134",
        transaction,
        "1234",
      )
    }
      .hasMessage("Offender trust account not found")
  }

  @Test
  fun testTransfer_offenderTrustAccountClosed() {
    val transaction = createTransferTransaction()

    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(createOffenderBooking()),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    whenever(offenderTrustAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderTrustAccount.builder().accountClosedFlag("Y").build()),
    )

    Assertions.assertThatThrownBy {
      financeService.transferToSavings(
        "LEI",
        "AA2134",
        transaction,
        "1234",
      )
    }
      .hasMessage("Offender trust account closed")
  }

  @Test
  fun testTransfer_offenderSubAccountNotFound() {
    val transaction = createTransferTransaction()

    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(createOffenderBooking()),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    whenever(offenderTrustAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()),
    )

    Assertions.assertThatThrownBy {
      financeService.transferToSavings(
        "LEI",
        "AA2134",
        transaction,
        "1234",
      )
    }
      .hasMessage("Offender sub account not found")
  }

  @Test
  fun testTransfer_offenderSubAccountBalanceNotEnough() {
    val transaction = createTransferTransaction()

    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(createOffenderBooking()),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    whenever(offenderTrustAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()),
    )
    whenever(offenderSubAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderSubAccount.builder().balance(BigDecimal("12")).build()),
    )

    Assertions.assertThatThrownBy {
      financeService.transferToSavings(
        "LEI",
        "AA2134",
        transaction,
        "1234",
      )
    }
      .hasMessage("Not enough money in offender sub account balance - 12.00")
  }

  @Test
  fun testTransfer_clientUniqueRefAlreadyUsed() {
    val transaction = createTransferTransaction()

    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(createOffenderBooking()),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    whenever(offenderTrustAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()),
    )
    whenever(offenderSubAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderSubAccount.builder().balance(BigDecimal("12.34")).build()),
    )

    whenever(offenderTransactionRepository.findByClientUniqueRef(ArgumentMatchers.anyString())).thenReturn(
      Optional.of(OffenderTransaction.builder().build()),
    )

    Assertions.assertThatThrownBy {
      financeService.transferToSavings(
        "LEI",
        "AA2134",
        transaction,
        "1234",
      )
    }
      .hasMessage("Duplicate post - The unique_client_ref clientRef has been used before")
  }

  @Test
  fun testTransfer() {
    val transaction = createTransferTransaction()

    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(createOffenderBooking()),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    whenever(offenderTrustAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()),
    )
    whenever(offenderSubAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderSubAccount.builder().balance(BigDecimal("12.34")).build()),
    )

    whenever(offenderTransactionRepository.nextTransactionId).thenReturn(12345L)
    whenever(offenderTransactionRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(
        OffenderTransaction.builder().build(),
      ),
    )

    val transfer = financeService.transferToSavings("LEI", "AA2134", transaction, "1234")
    assertThat(transfer.transactionId).isEqualTo(12345)
    assertThat(transfer.debitTransaction.id).isEqualTo("12345-1")
    assertThat(transfer.creditTransaction.id).isEqualTo("12345-2")
  }

  @Test
  fun testTransfer_setClientUniqueRef() {
    val transaction = createTransferTransaction()

    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(createOffenderBooking()),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    whenever(offenderTrustAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()),
    )
    whenever(offenderSubAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderSubAccount.builder().balance(BigDecimal("12.34")).build()),
    )

    whenever(offenderTransactionRepository.nextTransactionId).thenReturn(12345L)
    val transaction1 = OffenderTransaction.builder().build()
    val transaction2 = OffenderTransaction.builder().build()
    whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
      .thenReturn(Optional.of(transaction1))
      .thenReturn(Optional.of(transaction2))

    financeService.transferToSavings("LEI", "AA2134", transaction, "clientUniqueId")
    assertThat(transaction1.clientUniqueRef).isEqualTo("clientUniqueId")
    assertThat(transaction1.transactionReferenceNumber).isEqualTo("transId")
    assertThat(transaction2.clientUniqueRef).isNull()
    assertThat(transaction2.transactionReferenceNumber).isEqualTo("transId")
  }

  @Test
  fun testTransfer_verifyCalls() {
    val transaction = createTransferTransaction()

    whenever(
      offenderBookingRepository.findByOffenderNomsIdAndActive(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.anyBoolean(),
      ),
    ).thenReturn(
      Optional.of(createOffenderBooking()),
    )

    whenever(
      accountCodeRepository.findByCaseLoadTypeAndSubAccountType(
        ArgumentMatchers.anyString(),
        eq("SPND"),
      ),
    ).thenReturn(
      Optional.of(AccountCode.builder().accountCode(2101L).build()),
    )

    whenever(offenderTrustAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()),
    )
    whenever(offenderSubAccountRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(OffenderSubAccount.builder().balance(BigDecimal("12.34")).build()),
    )

    whenever(offenderTransactionRepository.findById(ArgumentMatchers.any())).thenReturn(
      Optional.of(
        OffenderTransaction.builder().build(),
      ),
    )
    whenever(offenderTransactionRepository.nextTransactionId).thenReturn(12345L)

    financeService.transferToSavings("LEI", "AA2134", transaction, "1234")

    verify(offenderBookingRepository).findByOffenderNomsIdAndActive("AA2134", true)
    verify(accountCodeRepository).findByCaseLoadTypeAndSubAccountType("INST", "SPND")

    verify(offenderTrustAccountRepository).findById(OffenderTrustAccount.Pk("LEI", 12L))
    verify(offenderSubAccountRepository).findById(OffenderSubAccount.Pk("LEI", 12L, 2101L))

    verify(financeRepository).insertIntoOffenderTrans(
      eq("LEI"),
      eq(12L),
      eq(1L),
      eq("DR"),
      eq("SPND"),
      eq(12345L),
      eq(1L),
      eq(BigDecimal("12.34")),
      eq("desc"),
      any(),
    )
    verify(financeRepository).insertIntoOffenderTrans(
      eq("LEI"),
      eq(12L),
      eq(1L),
      eq("CR"),
      eq("SAV"),
      eq(12345L),
      eq(2L),
      eq(BigDecimal("12.34")),
      eq("desc"),
      any(),
    )
    verify(financeRepository).processGlTransNew(
      eq("LEI"),
      eq(12L),
      eq(1L),
      eq("SPND"),
      eq("SAV"),
      eq(12345L),
      eq(1L),
      eq(BigDecimal("12.34")),
      eq("desc"),
      any(),
    )
  }

  private fun createOffenderBooking(): OffenderBooking = OffenderBooking.builder()
    .bookingId(1L)
    .rootOffender(Offender.builder().id(12L).build())
    .location(AgencyLocation.builder().id("LEI").build())
    .build()

  private fun createTransferTransaction(): TransferTransaction = TransferTransaction.builder()
    .amount(1234L)
    .clientUniqueRef("clientRef")
    .description("desc")
    .clientTransactionId("transId")
    .build()

  @Test
  fun test_getBalances_HappyPath() {
    val bookingId = -1L
    val offenderNo = "A1234AB"
    val agency = "LEI"

    val offenderSummary = OffenderSummary
      .builder()
      .offenderNo(offenderNo)
      .agencyLocationId(agency)
      .build()

    val offenderDamageObligationModel = OffenderDamageObligationModel
      .builder()
      .amountToPay(BigDecimal.valueOf(10))
      .amountPaid(BigDecimal.valueOf(5))
      .build()

    val account = Account.builder().build()

    whenever(bookingRepository.getLatestBookingByBookingId(bookingId))
      .thenReturn(Optional.of(offenderSummary))
    whenever(
      offenderDamageObligationService.getDamageObligations(
        offenderNo,
        OffenderDamageObligation.Status.ACTIVE,
      ),
    )
      .thenReturn(listOf(offenderDamageObligationModel))
    whenever(financeRepository.getBalances(bookingId, agency)).thenReturn(account)

    val accountToReturn = financeService.getBalances(bookingId)

    assertThat(accountToReturn).isNotNull()
    assertThat(accountToReturn.damageObligations).isEqualTo(MoneySupport.toMoneyScale(BigDecimal.valueOf(5)))

    verify(bookingRepository, times(1)).getLatestBookingByBookingId(bookingId)
    verify(offenderDamageObligationService, times(1))
      .getDamageObligations(offenderNo, OffenderDamageObligation.Status.ACTIVE)
    verify(financeRepository, times(1)).getBalances(bookingId, agency)
  }

  @Test
  fun test_getBalances_With_Two_Obligations() {
    val bookingId = -1L
    val offenderNo = "A1234AB"
    val agency = "LEI"

    val offenderSummary = OffenderSummary
      .builder()
      .offenderNo(offenderNo)
      .agencyLocationId(agency)
      .build()

    val offenderDamageObligationModel1 = OffenderDamageObligationModel
      .builder()
      .amountToPay(BigDecimal.valueOf(10))
      .amountPaid(BigDecimal.valueOf(3))
      .build()
    val offenderDamageObligationModel2 = OffenderDamageObligationModel
      .builder()
      .amountToPay(BigDecimal.valueOf(5))
      .amountPaid(BigDecimal.valueOf(2))
      .build()

    val account = Account.builder().build()

    whenever(bookingRepository.getLatestBookingByBookingId(bookingId))
      .thenReturn(Optional.of(offenderSummary))
    whenever(
      offenderDamageObligationService.getDamageObligations(
        offenderNo,
        OffenderDamageObligation.Status.ACTIVE,
      ),
    )
      .thenReturn(listOf(offenderDamageObligationModel1, offenderDamageObligationModel2))
    whenever(financeRepository.getBalances(bookingId, agency)).thenReturn(account)

    val accountToReturn = financeService.getBalances(bookingId)

    assertThat(accountToReturn).isNotNull()
    assertThat(accountToReturn.damageObligations)
      .isEqualTo(MoneySupport.toMoneyScale(BigDecimal.valueOf(10)))

    verify(bookingRepository, times(1)).getLatestBookingByBookingId(bookingId)
    verify(offenderDamageObligationService, times(1))
      .getDamageObligations(offenderNo, OffenderDamageObligation.Status.ACTIVE)
    verify(financeRepository, times(1)).getBalances(bookingId, agency)
  }

  @Test
  fun test_getBalances_And_No_OffenderSummary() {
    val bookingId = -1L

    whenever(bookingRepository.getLatestBookingByBookingId(bookingId))
      .thenReturn(Optional.empty())

    val exception: Throwable = assertThrows(
      EntityNotFoundException::class.java,
    ) { financeService.getBalances(bookingId) }

    assertThat(exception.message).isEqualTo("Booking not found for id: -1")
  }

  @Test
  fun test_getBalances_And_No_Account() {
    val bookingId = -1L
    val offenderNo = "A1234AB"
    val agency = "LEI"

    val offenderSummary = OffenderSummary
      .builder()
      .offenderNo(offenderNo)
      .agencyLocationId(agency)
      .build()

    whenever(bookingRepository.getLatestBookingByBookingId(bookingId))
      .thenReturn(Optional.of(offenderSummary))
    whenever(financeRepository.getBalances(bookingId, agency)).thenReturn(null)

    val accountToReturn = financeService.getBalances(bookingId)

    assertThat(accountToReturn).isNotNull()
    assertThat(accountToReturn.damageObligations).isEqualTo(MoneySupport.toMoney("0.00"))
    assertThat(accountToReturn.spends).isEqualTo(MoneySupport.toMoney("0.00"))
    assertThat(accountToReturn.cash).isEqualTo(MoneySupport.toMoney("0.00"))
    assertThat(accountToReturn.savings).isEqualTo(MoneySupport.toMoney("0.00"))
    assertThat(accountToReturn.currency).isEqualTo("GBP")

    verify(bookingRepository, times(1)).getLatestBookingByBookingId(bookingId)
    verify(financeRepository, times(1)).getBalances(bookingId, agency)
  }
}
