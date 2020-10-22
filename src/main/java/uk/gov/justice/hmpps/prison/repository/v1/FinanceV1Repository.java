package uk.gov.justice.hmpps.prison.repository.v1;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.v1.CodeDescription;
import uk.gov.justice.hmpps.prison.repository.RepositoryBase;
import uk.gov.justice.hmpps.prison.repository.v1.model.AccountTransactionSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.HoldSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.TransferSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.TransferSP.TransactionSP;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetAccountBalances;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetAccountTransactions;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetHolds;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetTransactionByClientUniqueRef;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostStorePayment;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostTransaction;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostTransfer;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ACCOUNT_TYPE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CASH_BALANCE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CLIENT_UNIQUE_REF;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CURRENT_AGY_DESC;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CURRENT_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_FROM_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_FROM_DATE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_HOLDS_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROOT_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_SAVINGS_BALANCE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_SINGLE_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_SPENDS_BALANCE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TO_DATE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TRANS_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_ENTRY_AMOUNT;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_ENTRY_DATE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_ENTRY_DESC;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_ENTRY_SEQ;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_REFERENCE_NUMBER;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_TYPE;

@Slf4j
@Repository
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FinanceV1Repository extends RepositoryBase {

    private final PostTransaction postTransactionProc;
    private final PostTransfer postTransferProc;
    private final GetHolds getHoldsProc;
    private final PostStorePayment postStorePaymentProc;
    private final GetAccountBalances getAccountBalancesProc;
    private final GetAccountTransactions getAccountTransactionsProc;
    private final GetTransactionByClientUniqueRef getTransactionByClientUniqueRefProc;

    public TransferSP postTransfer(final String prisonId, final String nomsId, final String type, final String description, final BigDecimal amountInPounds, final LocalDate txDate, final String txId, final String uniqueClientId) {
        final var params = new MapSqlParameterSource()
                .addValue(P_FROM_AGY_LOC_ID, prisonId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_TXN_TYPE, type)
                .addValue(P_TXN_REFERENCE_NUMBER, txId)
                .addValue(P_TXN_ENTRY_DESC, description)
                .addValue(P_TXN_ENTRY_AMOUNT, amountInPounds)
                .addValue(P_TXN_ENTRY_DATE, DateTimeConverter.toDate(txDate))
                .addValue(P_CLIENT_UNIQUE_REF, uniqueClientId);

        final var result = postTransferProc.execute(params);

        final var txnId = result.get(P_TXN_ID);
        final var txnEntrySeq = result.get(P_TXN_ENTRY_SEQ);

        return new TransferSP(CodeDescription.safeNullBuild((String) result.get(P_CURRENT_AGY_LOC_ID), (String) result.get(P_CURRENT_AGY_DESC)),
                new TransactionSP(txnId + "-" + txnEntrySeq));
    }

    public String postTransaction(final String prisonId, final String nomsId, final String type, final String description, final BigDecimal amountInPounds, final LocalDate txDate, final String txId, final String uniqueClientId) {
        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_TXN_TYPE, type)
                .addValue(P_TXN_REFERENCE_NUMBER, txId)
                .addValue(P_TXN_ENTRY_DESC, description)
                .addValue(P_TXN_ENTRY_AMOUNT, amountInPounds)
                .addValue(P_TXN_ENTRY_DATE, DateTimeConverter.toDate(txDate))
                .addValue(P_CLIENT_UNIQUE_REF, uniqueClientId);

        final var result = postTransactionProc.execute(params);

        final var txnId = result.get(P_TXN_ID);
        final var txnEntrySeq = result.get(P_TXN_ENTRY_SEQ);
        return txnId + "-" + txnEntrySeq;
    }

    public List<HoldSP> getHolds(final String prisonId, final String nomsId, final String uniqueClientId) {
        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_CLIENT_UNIQUE_REF, uniqueClientId);

        final var result = getHoldsProc.execute(params);

        //noinspection unchecked
        return (List<HoldSP>) result.get(P_HOLDS_CSR);
    }

    public void postStorePayment(final String prisonId, final String nomsId, final String payType, final String description, final BigDecimal payAmount, final LocalDate payDate, final String clientRef) {

        final var params = new MapSqlParameterSource()
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_TXN_TYPE, payType)
                .addValue(P_TXN_REFERENCE_NUMBER, clientRef)
                .addValue(P_TXN_ENTRY_DATE, DateTimeConverter.toDate(payDate))
                .addValue(P_TXN_ENTRY_DESC, description)
                .addValue(P_TXN_ENTRY_AMOUNT, payAmount);

        // No out parameters - a runtime exception will be thrown in the event of errors
        postStorePaymentProc.execute(params);
    }

    public Map<String, BigDecimal> getAccountBalances(final String prisonId, final String nomsId) {

        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null);

        final var result = getAccountBalancesProc.execute(params);

        return Map.of(
                "cash", (BigDecimal) result.get(P_CASH_BALANCE),
                "spends", (BigDecimal) result.get(P_SPENDS_BALANCE),
                "savings", (BigDecimal) result.get(P_SAVINGS_BALANCE));
    }


    public List<AccountTransactionSP> getAccountTransactions(final String prisonId, final String nomsId, final String accountType, final LocalDate fromDate, final LocalDate toDate) {

        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ACCOUNT_TYPE, accountType)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_CLIENT_UNIQUE_REF, null)
                .addValue(P_FROM_DATE, DateTimeConverter.toDate(fromDate))
                .addValue(P_TO_DATE, DateTimeConverter.toDate(toDate));

        final var result = getAccountTransactionsProc.execute(params);

        //noinspection: unchecked
        return (List<AccountTransactionSP>) result.get(P_TRANS_CSR);
    }

    public List<AccountTransactionSP> getTransactionByClientUniqueRef(final String prisonId, final String nomsId, final String uniqueClientID) {

        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ACCOUNT_TYPE, null)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_CLIENT_UNIQUE_REF, uniqueClientID)
                .addValue(P_FROM_DATE, null)
                .addValue(P_TO_DATE, null);

        final var result = getTransactionByClientUniqueRefProc.execute(params);

        //noinspection: unchecked
        return (List<AccountTransactionSP>) result.get(P_TRANS_CSR);
    }
}
