package uk.gov.justice.hmpps.prison.service;

import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction;
import uk.gov.justice.hmpps.prison.api.model.TransferTransactionDetail;
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction.Pk;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository;
import uk.gov.justice.hmpps.prison.values.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation.Status.ACTIVE;
import static uk.gov.justice.hmpps.prison.util.MoneySupport.toMoney;
import static uk.gov.justice.hmpps.prison.util.MoneySupport.toMoneyScale;
import static uk.gov.justice.hmpps.prison.values.AccountCode.SAVINGS;
import static uk.gov.justice.hmpps.prison.values.AccountCode.SPENDS;


@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Slf4j
public class FinanceService {

    private final FinanceRepository financeRepository;
    private final BookingRepository bookingRepository;
    private final OffenderBookingRepository offenderBookingRepository;
    private final OffenderTransactionRepository offenderTransactionRepository;
    private final AccountCodeRepository accountCodeRepository;
    private final OffenderSubAccountRepository offenderSubAccountRepository;
    private final OffenderTrustAccountRepository offenderTrustAccountRepository;
    private final OffenderDamageObligationService offenderDamageObligationService;
    private final Currency currency;

    public Account getBalances(final Long bookingId) {

        final var offenderSummary = bookingRepository.getLatestBookingByBookingId(bookingId)
            .orElseThrow(new EntityNotFoundException("Booking not found for id: " + bookingId));

        final var damageObligationBalance =
            offenderDamageObligationService.getDamageObligations(offenderSummary.getOffenderNo(), ACTIVE)
                .stream()
                .map(model -> model.getAmountToPay().subtract(model.getAmountPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Optional.ofNullable(financeRepository.getBalances(bookingId, offenderSummary.getAgencyLocationId()))
            .map(Account::toBuilder)
            .map(builder -> builder.damageObligations(toMoneyScale(damageObligationBalance)).build())
            .orElse(defaultBalances());
    }

    private Account defaultBalances() {
        final var zero = toMoney("0.00");
        return Account.builder()
            .spends(zero).cash(zero).savings(zero).damageObligations(zero)
            .currency(currency.getCode()).build();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('NOMIS_API_V1', 'UNILINK')")
    public TransferTransactionDetail transferToSavings(final String prisonId, final String offenderNo, final TransferTransaction transferTransaction,
                                                       final String clientUniqueId) {
        final var optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndActive(offenderNo, true);
        final var booking = optionalOffenderBooking.orElseThrow(EntityNotFoundException.withMessage("No active offender bookings found for offender number %s", offenderNo));

        final var subActTypeDr = SPENDS.code;
        final var subActTypeDrId = accountCodeRepository.findByCaseLoadTypeAndSubAccountType("INST", subActTypeDr).orElseThrow().getAccountCode();
        final var subActTypeCr = SAVINGS.code;

        validateTransferToSavings(prisonId, offenderNo, transferTransaction, booking, subActTypeDrId);

        final var transactionNumber = offenderTransactionRepository.getNextTransactionId();

        final var transferDate = new Date();

        financeRepository.insertIntoOffenderTrans(prisonId, booking.getRootOffender().getId(), booking.getBookingId(), "DR", subActTypeDr,
                transactionNumber, 1, transferTransaction.getAmountInPounds(), transferTransaction.getDescription(), transferDate);
        financeRepository.insertIntoOffenderTrans(prisonId, booking.getRootOffender().getId(), booking.getBookingId(), "CR", subActTypeCr,
                transactionNumber, 2, transferTransaction.getAmountInPounds(), transferTransaction.getDescription(), transferDate);

        final var offenderTransaction = offenderTransactionRepository.findById(new Pk(transactionNumber, 1L)).orElseThrow();
        offenderTransaction.setClientUniqueRef(clientUniqueId);
        offenderTransaction.setTransactionReferenceNumber(transferTransaction.getClientTransactionId());

        final var offenderTransaction2 = offenderTransactionRepository.findById(new Pk(transactionNumber, 2L)).orElseThrow();
        // client unique ref is unique on the table, so can only mark one of the transactions with the unique ref.
        offenderTransaction2.setTransactionReferenceNumber(transferTransaction.getClientTransactionId());

        financeRepository.processGlTransNew(prisonId, booking.getRootOffender().getId(), booking.getBookingId(), subActTypeDr, subActTypeCr,
                transactionNumber, 1, transferTransaction.getAmountInPounds(), transferTransaction.getDescription(), transferDate);

        return TransferTransactionDetail.builder()
                .debitTransaction(Transaction.builder().id(transactionNumber + "-1").build())
                .creditTransaction(Transaction.builder().id(transactionNumber + "-2").build())
                .transactionId(transactionNumber).build();
    }

    private void validateTransferToSavings(final String prisonId, final String offenderNo, final TransferTransaction transferTransaction, final OffenderBooking booking, final Long subActTypeDr) {
        if (!booking.getLocation().getId().equals(prisonId)) {
            throw EntityNotFoundException.withMessage("Offender %s found at prison %s instead of %s", offenderNo, booking.getLocation().getId(), prisonId);
        }

        final var optionalOffenderTrustAccount = offenderTrustAccountRepository.findById(new OffenderTrustAccount.Pk(prisonId, booking.getRootOffender().getId()));

        if (optionalOffenderTrustAccount.isEmpty()) {
            throw new ValidationException("Offender trust account not found");
        }
        if (optionalOffenderTrustAccount.get().isAccountClosed()) {
            throw new ValidationException("Offender trust account closed");
        }

        final var optionalOffenderSubAccount = offenderSubAccountRepository.findById(new OffenderSubAccount.Pk(prisonId, booking.getRootOffender().getId(), subActTypeDr));
        if (optionalOffenderSubAccount.isEmpty()) {
            throw new ValidationException("Offender sub account not found");
        }
        final var balance = optionalOffenderSubAccount.get().getBalance();
        if (balance.compareTo(transferTransaction.getAmountInPounds()) < 0) {
            throw new ValidationException(String.format("Not enough money in offender sub account balance - %s", balance.setScale(2, RoundingMode.HALF_UP)));
        }
    }
}
