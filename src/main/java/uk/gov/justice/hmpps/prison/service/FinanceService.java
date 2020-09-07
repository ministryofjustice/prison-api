package uk.gov.justice.hmpps.prison.service;

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
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AccountCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSubAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTrustAccountRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import javax.validation.ValidationException;

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

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Account getBalances(final Long bookingId) {
        return bookingRepository.getBookingAgency(bookingId)
                .map(agency -> financeRepository.getBalances(bookingId, agency))
                .orElse(null);
    }

    // @Transactional
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'NOMIS_API_V1')")
    public TransferTransactionDetail transferToSavings(final String prisonId, final String offenderNo, final TransferTransaction transferTransaction,
                                                       final String clientUniqueId) {
        final var optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(offenderNo, "Y");
        final var booking = optionalOffenderBooking.orElseThrow(EntityNotFoundException.withMessage("No active offender bookings found for offender number %s", offenderNo));

        final var subActTypeDr = accountCodeRepository.findByCaseLoadTypeAndSubAccountType("INST", "REG").orElseThrow().getAccountCode();
        final var subActTypeCr = accountCodeRepository.findByCaseLoadTypeAndSubAccountType("INST", "SAV").orElseThrow().getAccountCode();

        validateTransferToSavings(prisonId, offenderNo, transferTransaction, booking, subActTypeDr);

        final var transactionNumber = "12345";

//        // Get the next transaction number from the sequence
//        final var transactionNumber = offenderTransactionRepository.getNextTransactionId();
//
//        final var transferDate = new Date();
//
//        financeRepository.insertIntoOffenderTrans(prisonId, booking.getRootOffenderId(), booking.getBookingId(), "DR", subActTypeDr,
//                transactionNumber, 1, transferTransaction.getAmountInPounds(), transferTransaction.getDescription(), transferDate);
//        financeRepository.insertIntoOffenderTrans(prisonId, booking.getRootOffenderId(), booking.getBookingId(), "CR", subActTypeCr,
//                transactionNumber, 2, transferTransaction.getAmountInPounds(), transferTransaction.getDescription(), transferDate);
//
//        // need to work out if we just mark the first transaction with the unique reference?
//        //       UPDATE offender_transactions
//        //         SET TXN_REFERENCE_NUMBER = transferTransaction.,
//        //             CLIENT_UNIQUE_REF =  clientUniqueId,
//        //             txn_adjusted_flag = 'N',
//        //             hold_clear_flag = 'N'
//        //       WHERE txn_id = p_txn_num
//
//        financeRepository.processGlTransNew(prisonId, booking.getRootOffenderId(), booking.getBookingId(), subActTypeDr, subActTypeCr,
//                transactionNumber, 1, transferTransaction.getAmountInPounds(), transferTransaction.getDescription(), transferDate);

        return TransferTransactionDetail.builder()
                .debitTransaction(Transaction.builder().id(transactionNumber + "-1").build())
                .creditTransaction(Transaction.builder().id(transactionNumber + "-2").build())
                .transactionId(transactionNumber).build();
    }

    private void validateTransferToSavings(final String prisonId, final String offenderNo, final TransferTransaction transferTransaction, final OffenderBooking booking, final Long subActTypeDr) {
        if (!booking.getLocation().getId().equals(prisonId)) {
            throw EntityNotFoundException.withMessage("Offender %s found at prison %s instead of %s", offenderNo, booking.getLocation().getId(), prisonId);
        }

        final var optionalOffenderTrustAccount = offenderTrustAccountRepository.findById(new OffenderTrustAccount.Pk(prisonId, booking.getRootOffenderId()));

        if (optionalOffenderTrustAccount.isEmpty()) {
            throw new ValidationException("Offender trust account not found");
        }
        if (optionalOffenderTrustAccount.get().isAccountClosed()) {
            throw new ValidationException("Offender trust account closed");
        }

        final var optionalOffenderSubAccount = offenderSubAccountRepository.findById(new OffenderSubAccount.Pk(prisonId, booking.getRootOffenderId(), subActTypeDr));
        if (optionalOffenderSubAccount.isEmpty()) {
            throw new ValidationException("Offender sub account not found");
        }
        final var balance = optionalOffenderSubAccount.get().getBalance();
        if (balance.compareTo(transferTransaction.getAmountInPounds()) < 0) {
            throw new ValidationException(String.format("Not enough money in offender sub account balance - %s", balance));
        }
    }
}
