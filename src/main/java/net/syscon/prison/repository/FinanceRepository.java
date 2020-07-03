package net.syscon.prison.repository;

import net.syscon.prison.api.model.Account;

public interface FinanceRepository {

    Account getBalances(long bookingId);
}
