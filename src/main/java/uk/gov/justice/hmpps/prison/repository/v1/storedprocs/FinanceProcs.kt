package uk.gov.justice.hmpps.prison.repository.v1.storedprocs

import org.springframework.jdbc.core.SqlOutParameter
import org.springframework.jdbc.core.SqlParameter
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator
import uk.gov.justice.hmpps.prison.repository.v1.model.AccountTransactionSP
import uk.gov.justice.hmpps.prison.repository.v1.model.HoldSP
import java.sql.Types
import javax.sql.DataSource

@Component
class FinanceProcs {
  @Component
  class PostTransaction(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName(StoreProcMetadata.API_OWNER)
        .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
        .withProcedureName("post_transaction")
        .withNamedBinding()
        .declareParameters(
          SqlParameter(StoreProcMetadata.P_AGY_LOC_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
          SqlParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_TYPE, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_REFERENCE_NUMBER, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_DESC, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_AMOUNT, Types.NUMERIC),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_DATE, Types.DATE),
          SqlParameter(StoreProcMetadata.P_CLIENT_UNIQUE_REF, Types.VARCHAR),
          SqlOutParameter(StoreProcMetadata.P_TXN_ID, Types.INTEGER),
          SqlOutParameter(StoreProcMetadata.P_TXN_ENTRY_SEQ, Types.INTEGER),
        )
      compile()
    }
  }

  @Component
  class PostTransfer(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName(StoreProcMetadata.API_OWNER)
        .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
        .withProcedureName("post_transfer")
        .withNamedBinding()
        .declareParameters(
          SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
          SqlParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_FROM_AGY_LOC_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_TYPE, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_REFERENCE_NUMBER, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_DATE, Types.DATE),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_DESC, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_AMOUNT, Types.NUMERIC),
          SqlParameter(StoreProcMetadata.P_CLIENT_UNIQUE_REF, Types.VARCHAR),
          SqlOutParameter(StoreProcMetadata.P_CURRENT_AGY_LOC_ID, Types.VARCHAR),
          SqlOutParameter(StoreProcMetadata.P_CURRENT_AGY_DESC, Types.VARCHAR),
          SqlOutParameter(StoreProcMetadata.P_TXN_ID, Types.INTEGER),
          SqlOutParameter(StoreProcMetadata.P_TXN_ENTRY_SEQ, Types.INTEGER),
        )
      compile()
    }
  }

  @Component
  class GetHolds(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName(StoreProcMetadata.API_OWNER)
        .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
        .withProcedureName("holds")
        .withNamedBinding()
        .declareParameters(
          SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
          SqlParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_AGY_LOC_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_CLIENT_UNIQUE_REF, Types.VARCHAR),
        )
        .returningResultSet(
          StoreProcMetadata.P_HOLDS_CSR,
          StandardBeanPropertyRowMapper.newInstance<HoldSP>(HoldSP::class.java),
        )
      compile()
    }
  }

  @Component
  class PostStorePayment(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName(StoreProcMetadata.API_OWNER)
        .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
        .withProcedureName("store_payment")
        .withNamedBinding()
        .declareParameters(
          SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
          SqlParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_AGY_LOC_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_TYPE, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_REFERENCE_NUMBER, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_DATE, Types.DATE),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_DESC, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_TXN_ENTRY_AMOUNT, Types.NUMERIC),
        )
      compile()
    }
  }

  @Component
  class GetAccountBalances(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName(StoreProcMetadata.API_OWNER)
        .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
        .withProcedureName("account_balances")
        .withNamedBinding()
        .declareParameters(
          SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
          SqlParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_AGY_LOC_ID, Types.VARCHAR),
          SqlOutParameter(StoreProcMetadata.P_CASH_BALANCE, Types.NUMERIC),
          SqlOutParameter(StoreProcMetadata.P_SPENDS_BALANCE, Types.NUMERIC),
          SqlOutParameter(StoreProcMetadata.P_SAVINGS_BALANCE, Types.NUMERIC),
        )
      compile()
    }
  }

  @Component
  class GetAccountTransactions(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName(StoreProcMetadata.API_OWNER)
        .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
        .withProcedureName("transaction_history")
        .withNamedBinding()
        .declareParameters(
          SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
          SqlParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_AGY_LOC_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_ACCOUNT_TYPE, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_FROM_DATE, Types.DATE),
          SqlParameter(StoreProcMetadata.P_TO_DATE, Types.DATE),
        )
        .returningResultSet(
          StoreProcMetadata.P_TRANS_CSR,
          StandardBeanPropertyRowMapper.newInstance<AccountTransactionSP>(AccountTransactionSP::class.java),
        )
      compile()
    }
  }

  @Component
  class GetTransactionByClientUniqueRef(
    dataSource: DataSource,
    errorCodeTranslator: NomisV1SQLErrorCodeTranslator,
  ) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName(StoreProcMetadata.API_OWNER)
        .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
        .withProcedureName("transaction_history")
        .withNamedBinding()
        .declareParameters(
          SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_AGY_LOC_ID, Types.VARCHAR),
          SqlParameter(StoreProcMetadata.P_CLIENT_UNIQUE_REF, Types.VARCHAR),
        )
        .returningResultSet(
          StoreProcMetadata.P_TRANS_CSR,
          StandardBeanPropertyRowMapper.newInstance<AccountTransactionSP>(AccountTransactionSP::class.java),
        )
      compile()
    }
  }
}
