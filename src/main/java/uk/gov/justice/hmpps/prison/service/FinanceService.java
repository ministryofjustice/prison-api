package uk.gov.justice.hmpps.prison.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

@Service
@Transactional(readOnly = true)
public class FinanceService {
    private final FinanceRepository financeRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public FinanceService(final FinanceRepository financeRepository, final BookingRepository bookingRepository) {
        this.financeRepository = financeRepository;
        this.bookingRepository = bookingRepository;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Account getBalances(final Long bookingId) {
        return bookingRepository.getBookingAgency(bookingId)
                .map(agency -> financeRepository.getBalances(bookingId, agency))
                .orElse(null);
    }
}
