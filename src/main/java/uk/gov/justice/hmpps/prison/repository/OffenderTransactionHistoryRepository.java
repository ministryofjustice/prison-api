package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionHistory;

import java.time.LocalDate;
import java.util.List;

public interface OffenderTransactionHistoryRepository {

    List<TransactionHistory> getTransactionsHistory(final String prisonId, final String nomisId,
                                                    final String accountCode, final LocalDate fromDate,
                                                    final LocalDate toDate);
}