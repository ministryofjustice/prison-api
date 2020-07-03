package uk.gov.justice.hmpps.prison.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

@Service
@Transactional(readOnly = true)
public class FinanceService {
    private final FinanceRepository financeRepository;

    @Autowired
    public FinanceService(final FinanceRepository financeRepository) {
        this.financeRepository = financeRepository;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Account getBalances(final Long bookingId) {
        return financeRepository.getBalances(bookingId);
    }
}
