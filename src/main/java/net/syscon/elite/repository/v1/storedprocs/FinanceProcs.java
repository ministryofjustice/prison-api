package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.NomisV1SQLErrorCodeTranslator;
import net.syscon.elite.repository.v1.model.HoldSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

@Component
public class FinanceProcs {

    public static final String P_AGY_LOC_ID = "p_agy_loc_id";
    public static final String P_FROM_AGY_LOC_ID = "p_from_agy_loc_id";
    public static final String P_TXN_TYPE = "p_txn_type";
    public static final String P_TXN_REFERENCE_NUMBER = "p_txn_reference_number";
    public static final String P_TXN_ENTRY_DESC = "p_txn_entry_desc";
    public static final String P_TXN_ENTRY_AMOUNT = "p_txn_entry_amount";
    public static final String P_TXN_ENTRY_DATE = "p_txn_entry_date";
    public static final String P_CLIENT_UNIQUE_REF = "p_client_unique_ref";

    public static final String P_TXN_ID = "p_txn_id";
    public static final String P_TXN_ENTRY_SEQ = "p_txn_entry_seq";
    public static final String P_CURRENT_AGY_LOC_ID = "p_current_agy_loc_id";
    public static final String P_CURRENT_AGY_DESC = "p_current_agy_desc";

    private static final String API_FINANCE_PROCS = "api_finance_procs";

    @Component
    public static class PostTransaction extends SimpleJdbcCallWithExceptionTranslater {
        public PostTransaction(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("post_transaction")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_TXN_TYPE, Types.VARCHAR),
                            new SqlParameter(P_TXN_REFERENCE_NUMBER, Types.VARCHAR),
                            new SqlParameter(P_TXN_ENTRY_DESC, Types.VARCHAR),
                            new SqlParameter(P_TXN_ENTRY_AMOUNT, Types.NUMERIC),
                            new SqlParameter(P_TXN_ENTRY_DATE, Types.DATE),
                            new SqlParameter(P_CLIENT_UNIQUE_REF, Types.VARCHAR),
                            new SqlOutParameter(P_TXN_ID, Types.INTEGER),
                            new SqlOutParameter(P_TXN_ENTRY_SEQ, Types.INTEGER));
            compile();
        }
    }

    @Component
    public static class PostTransfer extends SimpleJdbcCallWithExceptionTranslater {
        public PostTransfer(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("holds")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_CLIENT_UNIQUE_REF, Types.VARCHAR),
                            new SqlOutParameter(P_CURRENT_AGY_LOC_ID, Types.VARCHAR),
                            new SqlOutParameter(P_CURRENT_AGY_DESC, Types.VARCHAR),
                            new SqlOutParameter(P_TXN_ID, Types.INTEGER),
                            new SqlOutParameter(P_TXN_ENTRY_SEQ, Types.INTEGER));
            compile();
        }
    }

    @Component
    public static class GetHolds extends SimpleJdbcCallWithExceptionTranslater {
        public GetHolds(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("holds")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_FROM_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_CLIENT_UNIQUE_REF, Types.VARCHAR))
                    .returningResultSet(P_HOLDS_CSR,
                            StandardBeanPropertyRowMapper.newInstance(HoldSP.class));
            compile();
        }
    }
}
