package net.syscon.prison.service;

import net.syscon.prison.api.model.Account;
import net.syscon.prison.repository.FinanceRepository;
import net.syscon.prison.security.VerifyBookingAccess;
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
