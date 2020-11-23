package uk.gov.justice.hmpps.prison.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.api.model.OffenderDamageObligationModel;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository;
import uk.gov.justice.hmpps.prison.values.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.util.MoneySupport.toMoneyScale;
import static uk.gov.justice.hmpps.prison.util.MoneySupport.toMoney;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation.Status.ACTIVE;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {
    @Mock
    private FinanceRepository financeRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private OffenderBookingRepository offenderBookingRepository;
    @Mock
    private OffenderTransactionRepository offenderTransactionRepository;
    @Mock
    private AccountCodeRepository accountCodeRepository;
    @Mock
    private OffenderSubAccountRepository offenderSubAccountRepository;
    @Mock
    private OffenderTrustAccountRepository offenderTrustAccountRepository;
    @Mock
    private OffenderDamageObligationService offenderDamageObligationService;

    private Currency currency = Currency.builder().code("GBP").build();

    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        financeService = new FinanceService(currency, financeRepository, bookingRepository, offenderBookingRepository, offenderTransactionRepository, accountCodeRepository,
                offenderSubAccountRepository, offenderTrustAccountRepository, offenderDamageObligationService);
    }

    @Test
    void testTransfer_offenderNotFound() {
        final var transaction = createTransferTransaction();
        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("No active offender bookings found for offender number AA2134");
    }

    @Test
    void testTransfer_wrongPrison() {
        final var transaction = createTransferTransaction();

        final var offenderBooking = createOffenderBooking();
        offenderBooking.getLocation().setId("WRONG_PRISON");
        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(offenderBooking));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));

        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("Offender AA2134 found at prison WRONG_PRISON instead of LEI");
    }

    @Test
    void testTransfer_offenderTrustAccountNotFound() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));

        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("Offender trust account not found");
    }

    @Test
    void testTransfer_offenderTrustAccountClosed() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("Y").build()));

        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("Offender trust account closed");
    }

    @Test
    void testTransfer_offenderSubAccountNotFound() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()));

        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("Offender sub account not found");
    }

    @Test
    void testTransfer_offenderSubAccountBalanceNotEnough() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()));
        when(offenderSubAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderSubAccount.builder().balance(new BigDecimal("12")).build()));

        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("Not enough money in offender sub account balance - 12.00");
    }

    @Test
    void testTransfer() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()));
        when(offenderSubAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderSubAccount.builder().balance(new BigDecimal("12.34")).build()));

        when(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L);
        when(offenderTransactionRepository.findById(any())).thenReturn(Optional.of(
                OffenderTransaction.builder().build()));

        final var transfer = financeService.transferToSavings("LEI", "AA2134", transaction, "1234");
        assertThat(transfer.getTransactionId()).isEqualTo(12345);
        assertThat(transfer.getDebitTransaction().getId()).isEqualTo("12345-1");
        assertThat(transfer.getCreditTransaction().getId()).isEqualTo("12345-2");
    }

    @Test
    void testTransfer_setClientUniqueRef() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()));
        when(offenderSubAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderSubAccount.builder().balance(new BigDecimal("12.34")).build()));

        when(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L);
        final var transaction1 = OffenderTransaction.builder().build();
        final var transaction2 = OffenderTransaction.builder().build();
        when(offenderTransactionRepository.findById(any()))
                .thenReturn(Optional.of(transaction1))
                .thenReturn(Optional.of(transaction2));

        financeService.transferToSavings("LEI", "AA2134", transaction, "clientUniqueId");
        assertThat(transaction1.getClientUniqueRef()).isEqualTo("clientUniqueId");
        assertThat(transaction1.getTransactionReferenceNumber()).isEqualTo("transId");
        assertThat(transaction2.getClientUniqueRef()).isNull();
        assertThat(transaction2.getTransactionReferenceNumber()).isEqualTo("transId");
    }

    @Test
    void testTransfer_verifyCalls() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SPND"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()));
        when(offenderSubAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderSubAccount.builder().balance(new BigDecimal("12.34")).build()));

        when(offenderTransactionRepository.findById(any())).thenReturn(Optional.of(
                OffenderTransaction.builder().build()));
        when(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L);

        financeService.transferToSavings("LEI", "AA2134", transaction, "1234");

        verify(offenderBookingRepository).findByOffenderNomsIdAndActiveFlag("AA2134", "Y");
        verify(accountCodeRepository).findByCaseLoadTypeAndSubAccountType("INST", "SPND");

        verify(offenderTrustAccountRepository).findById(new OffenderTrustAccount.Pk("LEI", 12L));
        verify(offenderSubAccountRepository).findById(new OffenderSubAccount.Pk("LEI", 12L, 2101L));

        verify(financeRepository).insertIntoOffenderTrans(eq("LEI"), eq(12L), eq(1L), eq("DR"), eq("SPND"), eq(12345L), eq(1L), eq(new BigDecimal("12.34")), eq("desc"), any());
        verify(financeRepository).insertIntoOffenderTrans(eq("LEI"), eq(12L), eq(1L), eq("CR"), eq("SAV"), eq(12345L), eq(2L), eq(new BigDecimal("12.34")), eq("desc"), any());
        verify(financeRepository).processGlTransNew(eq("LEI"), eq(12L), eq(1L), eq("SPND"), eq("SAV"), eq(12345L), eq(1L), eq(new BigDecimal("12.34")), eq("desc"), any());
    }

    @NotNull
    private OffenderBooking createOffenderBooking() {
        return OffenderBooking.builder()
                .bookingId(1L)
                .rootOffenderId(12L)
                .location(AgencyLocation.builder().id("LEI").build())
                .build();
    }

    @NotNull
    private TransferTransaction createTransferTransaction() {
        return TransferTransaction.builder()
                .amount(1234L)
                .clientUniqueRef("clientRef")
                .description("desc")
                .clientTransactionId("transId")
                .build();
    }

    @Test
    public void test_getBalances_HappyPath() {

        var bookingId = -1L;
        var offenderNo = "A1234AB";
        var agency = "LEI";

        var offenderSummary = OffenderSummary
            .builder()
            .offenderNo(offenderNo)
            .agencyLocationId(agency)
            .build();

        var offenderDamageObligationModel = OffenderDamageObligationModel
            .builder()
            .amountToPay(BigDecimal.valueOf(10))
            .build();

        var account = Account.builder().build();

        when(bookingRepository.getLatestBookingByBookingId(bookingId))
            .thenReturn(Optional.of(offenderSummary));
        when(offenderDamageObligationService.getDamageObligations(offenderNo, ACTIVE.name()))
            .thenReturn(List.of(offenderDamageObligationModel));
        when(financeRepository.getBalances(bookingId, agency)).thenReturn(account);

        var accountToReturn = financeService.getBalances(bookingId);

        assertThat(accountToReturn).isNotNull();
        assertThat(accountToReturn.getDamageObligations()).isEqualTo(toMoneyScale(BigDecimal.valueOf(10)));

        verify(bookingRepository, times(1)).getLatestBookingByBookingId(bookingId);
        verify(offenderDamageObligationService, times(1)).getDamageObligations(offenderNo, ACTIVE.name());
        verify(financeRepository, times(1)).getBalances(bookingId, agency);

    }

    @Test
    public void test_getBalances_With_Two_Obligations() {

        var bookingId = -1L;
        var offenderNo = "A1234AB";
        var agency = "LEI";

        var offenderSummary = OffenderSummary
            .builder()
            .offenderNo(offenderNo)
            .agencyLocationId(agency)
            .build();

        var offenderDamageObligationModel1 = OffenderDamageObligationModel
            .builder()
            .amountToPay(BigDecimal.valueOf(10))
            .build();
        var offenderDamageObligationModel2 = OffenderDamageObligationModel
            .builder()
            .amountToPay(BigDecimal.valueOf(5))
            .build();

        var account = Account.builder().build();

        when(bookingRepository.getLatestBookingByBookingId(bookingId))
            .thenReturn(Optional.of(offenderSummary));
        when(offenderDamageObligationService.getDamageObligations(offenderNo,  ACTIVE.name()))
            .thenReturn(List.of(offenderDamageObligationModel1, offenderDamageObligationModel2));
        when(financeRepository.getBalances(bookingId, agency)).thenReturn(account);

        var accountToReturn = financeService.getBalances(bookingId);

        assertThat(accountToReturn).isNotNull();
        assertThat(accountToReturn.getDamageObligations()).isEqualTo(toMoneyScale(BigDecimal.valueOf(15)));

        verify(bookingRepository, times(1)).getLatestBookingByBookingId(bookingId);
        verify(offenderDamageObligationService, times(1)).getDamageObligations(offenderNo, ACTIVE.name());
        verify(financeRepository, times(1)).getBalances(bookingId, agency);
    }

    @Test
    public void test_getBalances_And_No_OffenderSummary() {

        var bookingId = -1L;

        when(bookingRepository.getLatestBookingByBookingId(bookingId))
            .thenReturn(Optional.empty());

        Throwable exception = assertThrows(EntityNotFoundException.class, () -> {
            financeService.getBalances(bookingId);
        });

        assertThat(exception.getMessage()).isEqualTo("Booking not found for id: -1");
    }

    @Test
    public void test_getBalances_And_No_Account() {

        var bookingId = -1L;
        var offenderNo = "A1234AB";
        var agency = "LEI";

        var offenderSummary = OffenderSummary
            .builder()
            .offenderNo(offenderNo)
            .agencyLocationId(agency)
            .build();

        when(bookingRepository.getLatestBookingByBookingId(bookingId))
            .thenReturn(Optional.of(offenderSummary));
        when(financeRepository.getBalances(bookingId, agency)).thenReturn(null);

        var accountToReturn = financeService.getBalances(bookingId);

        assertThat(accountToReturn).isNotNull();
        assertThat(accountToReturn.getDamageObligations()).isEqualTo(toMoney("0.00"));
        assertThat(accountToReturn.getSpends()).isEqualTo(toMoney("0.00"));
        assertThat(accountToReturn.getCash()).isEqualTo(toMoney("0.00"));
        assertThat(accountToReturn.getSavings()).isEqualTo(toMoney("0.00"));
        assertThat(accountToReturn.getCurrency()).isEqualTo("GBP");

        verify(bookingRepository, times(1)).getLatestBookingByBookingId(bookingId);
        verify(financeRepository, times(1)).getBalances(bookingId, agency);
    }
}
