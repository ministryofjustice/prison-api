package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.v1.NomisV1SQLErrorCodeTranslator;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

@Component
public class FinanceProcs {

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
                            new SqlParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
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
                    .withProcedureName("post_transfer")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_FROM_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_TXN_TYPE, Types.VARCHAR),
                            new SqlParameter(P_TXN_REFERENCE_NUMBER, Types.VARCHAR),
                            new SqlParameter(P_TXN_ENTRY_DATE, Types.DATE),
                            new SqlParameter(P_TXN_ENTRY_DESC, Types.VARCHAR),
                            new SqlParameter(P_TXN_ENTRY_AMOUNT, Types.NUMERIC),
                            new SqlParameter(P_CLIENT_UNIQUE_REF, Types.VARCHAR),
                            new SqlOutParameter(P_CURRENT_AGY_LOC_ID, Types.VARCHAR),
                            new SqlOutParameter(P_CURRENT_AGY_DESC, Types.VARCHAR),
                            new SqlOutParameter(P_TXN_ID, Types.INTEGER),
                            new SqlOutParameter(P_TXN_ENTRY_SEQ, Types.INTEGER));
            compile();
        }
    }


}
