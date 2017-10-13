package net.syscon.elite.persistence;

import java.util.Optional;
import java.util.Set;

import net.syscon.elite.api.model.Account;

public interface FinanceRepository {

    Optional<Account> getAccount(final long bookingId, final Set<String> caseloads);

}
