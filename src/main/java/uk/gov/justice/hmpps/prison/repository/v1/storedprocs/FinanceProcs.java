package uk.gov.justice.hmpps.prison.repository.v1.storedprocs;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import uk.gov.justice.hmpps.prison.repository.v1.model.AccountTransactionSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.HoldSP;

import javax.sql.DataSource;
import java.sql.Types;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.API_FINANCE_PROCS;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.API_OWNER;
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

@Component
public class FinanceProcs {

    @Component
    public static class PostTransaction extends SimpleJdbcCallWithExceptionTranslater {
        public PostTransaction(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
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
            withSchemaName(API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("post_transfer")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
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

    @Component
    public static class GetHolds extends SimpleJdbcCallWithExceptionTranslater {
        public GetHolds(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("holds")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_CLIENT_UNIQUE_REF, Types.VARCHAR))
                    .returningResultSet(P_HOLDS_CSR,
                            StandardBeanPropertyRowMapper.newInstance(HoldSP.class));
            compile();
        }
    }

    @Component
    public static class PostStorePayment extends SimpleJdbcCallWithExceptionTranslater {
        public PostStorePayment(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("store_payment")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_TXN_TYPE, Types.VARCHAR),
                            new SqlParameter(P_TXN_REFERENCE_NUMBER, Types.VARCHAR),
                            new SqlParameter(P_TXN_ENTRY_DATE, Types.DATE),
                            new SqlParameter(P_TXN_ENTRY_DESC, Types.VARCHAR),
                            new SqlParameter(P_TXN_ENTRY_AMOUNT, Types.NUMERIC));
            compile();
        }
    }

    @Component
    public static class GetAccountBalances extends SimpleJdbcCallWithExceptionTranslater {
        public GetAccountBalances(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("account_balances")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlOutParameter(P_CASH_BALANCE, Types.NUMERIC),
                            new SqlOutParameter(P_SPENDS_BALANCE, Types.NUMERIC),
                            new SqlOutParameter(P_SAVINGS_BALANCE, Types.NUMERIC));
            compile();
        }
    }

    @Component
    public static class GetAccountTransactions extends SimpleJdbcCallWithExceptionTranslater {
        public GetAccountTransactions(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("transaction_history")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_ACCOUNT_TYPE, Types.VARCHAR),
                            new SqlParameter(P_FROM_DATE, Types.DATE),
                            new SqlParameter(P_TO_DATE, Types.DATE))
                    .returningResultSet(P_TRANS_CSR,
                            StandardBeanPropertyRowMapper.newInstance(AccountTransactionSP.class));
            compile();
        }
    }

    @Component
    public static class GetTransactionByClientUniqueRef extends SimpleJdbcCallWithExceptionTranslater {
        public GetTransactionByClientUniqueRef(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_FINANCE_PROCS)
                    .withProcedureName("transaction_history")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_CLIENT_UNIQUE_REF, Types.VARCHAR))
                    .returningResultSet(P_TRANS_CSR,
                            StandardBeanPropertyRowMapper.newInstance(AccountTransactionSP.class));
            compile();
        }
    }


}
