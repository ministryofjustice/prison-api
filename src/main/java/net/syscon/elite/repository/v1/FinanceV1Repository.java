package net.syscon.elite.repository.v1;

import lombok.AllArgsConstructor;
import net.syscon.elite.api.model.v1.CodeDescription;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.HoldSP;
import net.syscon.elite.repository.v1.model.TransferSP;
import net.syscon.elite.repository.v1.model.TransferSP.TransactionSP;
import net.syscon.util.DateTimeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

@Repository
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FinanceV1Repository extends RepositoryBase {

    private final PostTransaction postTransactionProc;
    private final PostTransfer postTransferProc;
    private final GetHolds getHoldsProc;
    private final PostStorePayment postStorePaymentProc;

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

    public String postStorePayment(final String prisonId, final String nomsId, final String type, final String description, final BigDecimal amountInPounds, final LocalDate txDate, final String clientRef) {

        final var params = new MapSqlParameterSource()
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_TXN_TYPE, type)
                .addValue(P_TXN_REFERENCE_NUMBER, clientRef)
                .addValue(P_TXN_ENTRY_DATE, DateTimeConverter.toDate(txDate))
                .addValue(P_TXN_ENTRY_DESC, description)
                .addValue(P_TXN_ENTRY_AMOUNT, amountInPounds);

        final var result = postStorePaymentProc.execute(params);

        // TODO - examine result here - no OUT parameters

        return "OK";
    }
}
