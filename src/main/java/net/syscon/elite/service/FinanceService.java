package net.syscon.elite.service;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.repository.FinanceRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
