package net.syscon.elite.persistence;

import net.syscon.elite.api.model.Account;

public interface FinanceRepository {

    Account getBalances(long bookingId);
}
