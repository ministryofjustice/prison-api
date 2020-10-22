package uk.gov.justice.hmpps.prison.repository.impl;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionHistory;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.*;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TRANS_CSR;

@Repository
public class OffenderTransactionHistoryRepositoryImpl implements OffenderTransactionHistoryRepository {

    private final FinanceProcs.GetAccountTransactions getAccountTransactionsProc;

    public OffenderTransactionHistoryRepositoryImpl(FinanceProcs.GetAccountTransactions getAccountTransactionsProc) {
        this.getAccountTransactionsProc = getAccountTransactionsProc;
    }

    @Override
    public List<TransactionHistory> getTransactionsHistory(String prisonId, String nomisId, String accountCode, LocalDate fromDate, LocalDate toDate) {
        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomisId)
                .addValue(P_FROM_DATE, DateTimeConverter.toDate(fromDate))
                .addValue(P_TO_DATE, DateTimeConverter.toDate(toDate));

        final var paramsToUse = ofNullable(accountCode)
                .map(code -> params.addValue(P_ACCOUNT_TYPE, code))
                .orElse(params);

        final var result = getAccountTransactionsProc.execute(paramsToUse);

        return (List<TransactionHistory>) result.get(P_TRANS_CSR);
    }
}
