package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.repository.FinanceRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FinanceServiceImpl implements FinanceService {

    private final FinanceRepository financeRepository;
    private final BookingService bookingService;

    @Autowired
    public FinanceServiceImpl(FinanceRepository financeRepository, BookingService bookingService) {
        this.financeRepository = financeRepository;
        this.bookingService = bookingService;
    }

    @Override
    public Account getBalances(final long bookingId) {
        bookingService.verifyBookingAccess(bookingId);
        return financeRepository.getBalances(bookingId);
    }
}
