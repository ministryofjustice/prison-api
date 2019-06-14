package net.syscon.elite.repository.v1;

import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.LatestBookingSP;
import net.syscon.elite.repository.v1.model.OffenderSP;
import net.syscon.elite.repository.v1.storedprocs.GetLatestBookingProc;
import net.syscon.elite.repository.v1.storedprocs.GetOffenderDetailsProc;
import net.syscon.elite.repository.v1.storedprocs.PostTransactionProc;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.v1.storedprocs.PostTransactionProc.*;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

@Repository
public class NomisApiV1Repository extends RepositoryBase {

    private final GetLatestBookingProc getLatestBookingProc;
    private final GetOffenderDetailsProc getOffenderDetailsProc;
    private final PostTransactionProc postTransactionProc;

    public NomisApiV1Repository(GetLatestBookingProc getLatestBookingProc,
                                GetOffenderDetailsProc getOffenderDetailsProc,
                                NomisV1SQLErrorCodeTranslator errorCodeTranslator, PostTransactionProc postTransactionProc) {
        this.getLatestBookingProc = getLatestBookingProc;
        this.getOffenderDetailsProc = getOffenderDetailsProc;
        this.postTransactionProc = postTransactionProc;

        //TODO: There will be a better way of doing this...
        this.getLatestBookingProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
        this.getOffenderDetailsProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
        this.postTransactionProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
    }

    public Optional<LatestBookingSP> getLatestBooking(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getLatestBookingProc.execute(param);
        var locations = (List<LatestBookingSP>) result.get(P_BOOKING_CSR);

        return Optional.ofNullable(locations.isEmpty() ? null : locations.get(0));
    }

    public Optional<OffenderSP> getOffender(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderDetailsProc.execute(param);
        var offender = (List<OffenderSP>) result.get(P_OFFENDER_CSR);

        return Optional.ofNullable(offender.isEmpty() ? null : offender.get(0));
    }

    public String postTransaction(String prisonId, String nomsId, String type, String description, BigDecimal amountInPounds, LocalDate txDate, String txId, String uniqueClientId) {
        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_TXN_TYPE, type)
                .addValue(P_TXN_REFERENCE_NUMBER, txId)
                .addValue(P_TXN_ENTRY_DESC, description)
                .addValue(P_TXN_ENTRY_AMOUNT, amountInPounds)
                .addValue(P_TXN_ENTRY_DATE, txDate)
                .addValue(P_CLIENT_UNIQUE_REF, uniqueClientId);

        final var result = postTransactionProc.execute(params);

        final var txnId = (Integer)result.get(P_TXN_ID);
        final var txnEntrySeq =  (Integer) result.get(P_TXN_ENTRY_SEQ);
        return txnId + "-" + txnEntrySeq;
    }
}
