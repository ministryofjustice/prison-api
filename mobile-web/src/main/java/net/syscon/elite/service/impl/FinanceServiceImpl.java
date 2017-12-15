package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.repository.FinanceRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FinanceServiceImpl implements FinanceService {

    private final FinanceRepository financeRepository;

    @Autowired
    public FinanceServiceImpl(FinanceRepository financeRepository, BookingService bookingService) {
        this.financeRepository = financeRepository;
    }

    @Override
    @VerifyBookingAccess
    public Account getBalances(Long bookingId) {
        return financeRepository.getBalances(bookingId);
    }
}
