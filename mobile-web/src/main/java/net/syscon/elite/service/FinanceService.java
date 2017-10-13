package net.syscon.elite.service;

import net.syscon.elite.api.model.Account;

public interface FinanceService {

    Account getAccount(final long bookingId);
}
