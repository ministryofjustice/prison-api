package uk.gov.justice.hmpps.prison.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        financeService = new FinanceService(financeRepository, bookingRepository, offenderBookingRepository, offenderTransactionRepository, accountCodeRepository,
                offenderSubAccountRepository, offenderTrustAccountRepository);
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

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("REG"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));
        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SAV"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2102L).build()));

        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("Offender AA2134 found at prison WRONG_PRISON instead of LEI");
    }

    @Test
    void testTransfer_offenderTrustAccountNotFound() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("REG"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));
        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SAV"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2102L).build()));

        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("Offender trust account not found");
    }

    @Test
    void testTransfer_offenderTrustAccountClosed() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("REG"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));
        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SAV"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2102L).build()));

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

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("REG"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));
        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SAV"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2102L).build()));

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

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("REG"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));
        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SAV"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2102L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()));
        when(offenderSubAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderSubAccount.builder().balance(new BigDecimal("12.33")).build()));

        assertThatThrownBy(() -> financeService.transferToSavings("LEI", "AA2134", transaction, "1234"))
                .hasMessage("Not enough money in offender sub account balance - 12.33");
    }

    @Test
    void testTransfer() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("REG"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));
        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SAV"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2102L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()));
        when(offenderSubAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderSubAccount.builder().balance(new BigDecimal("12.34")).build()));

        final var transfer = financeService.transferToSavings("LEI", "AA2134", transaction, "1234");
        assertThat(transfer.getTransactionId()).isEqualTo("12345");
        assertThat(transfer.getDebitTransaction().getId()).isEqualTo("12345-1");
        assertThat(transfer.getCreditTransaction().getId()).isEqualTo("12345-2");
    }

    @Test
    void testTransfer_verifyCalls() {
        final var transaction = createTransferTransaction();

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(anyString(), anyString())).thenReturn(
                Optional.of(createOffenderBooking()));

        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("REG"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2101L).build()));
        when(accountCodeRepository.findByCaseLoadTypeAndSubAccountType(anyString(), eq("SAV"))).thenReturn(
                Optional.of(AccountCode.builder().accountCode(2102L).build()));

        when(offenderTrustAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderTrustAccount.builder().accountClosedFlag("N").build()));
        when(offenderSubAccountRepository.findById(any())).thenReturn(
                Optional.of(OffenderSubAccount.builder().balance(new BigDecimal("12.34")).build()));

        financeService.transferToSavings("LEI", "AA2134", transaction, "1234");

        verify(offenderBookingRepository).findByOffenderNomsIdAndActiveFlag("AA2134", "Y");
        verify(accountCodeRepository).findByCaseLoadTypeAndSubAccountType("INST", "REG");
        verify(accountCodeRepository).findByCaseLoadTypeAndSubAccountType("INST", "SAV");

        verify(offenderTrustAccountRepository).findById(new OffenderTrustAccount.Pk("LEI", 12L));
        verify(offenderSubAccountRepository).findById(new OffenderSubAccount.Pk("LEI", 12L, 2101L));
    }

    private OffenderBooking createOffenderBooking() {
        return OffenderBooking.builder()
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
}
