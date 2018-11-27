package net.syscon.elite.repository;

import net.syscon.elite.api.model.Account;

public interface FinanceRepository {

    Account getBalances(long bookingId);
}
