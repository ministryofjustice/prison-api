package net.syscon.elite.repository.v1;

import net.syscon.elite.api.model.v1.CodeDescription;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.TransferSP;
import net.syscon.elite.repository.v1.model.TransferSP.TransactionSP;
import net.syscon.util.DateTimeConverter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

@Repository
public class FinanceV1Repository extends RepositoryBase {

    private final PostTransaction postTransactionProc;
    private final PostTransfer postTransferProc;

    public FinanceV1Repository(final PostTransaction postTransactionProc, final PostTransfer postTransferProc) {
        this.postTransactionProc = postTransactionProc;
        this.postTransferProc = postTransferProc;
    }

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
}
