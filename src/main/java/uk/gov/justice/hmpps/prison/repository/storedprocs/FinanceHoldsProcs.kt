package uk.gov.justice.hmpps.prison.repository.storedprocs

import org.springframework.jdbc.core.SqlOutParameter
import org.springframework.jdbc.core.SqlParameter
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata
import java.sql.Types
import javax.sql.DataSource

@Component
class AddFinanceHold(dataSource: DataSource) : SimpleJdbcCall(dataSource) {
  init {
    withSchemaName(StoreProcMetadata.API_OWNER)
      .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
      .withProcedureName("post_hold")
      .withoutProcedureColumnMetaDataAccess()
      .withNamedBinding()
      .declareParameters(
        SqlParameter("p_noms_id", Types.VARCHAR),
        SqlParameter("p_root_offender_id", Types.NUMERIC),
        SqlParameter("p_single_offender_id", Types.NUMERIC),
        SqlParameter("p_agy_loc_id", Types.VARCHAR),
        // SqlParameter("p_txn_reference_number", Types.NUMERIC),
        // SqlParameter("p_txn_entry_date", Types.DATE),
        SqlParameter("p_txn_entry_desc", Types.VARCHAR),
        SqlParameter("p_txn_entry_amount", Types.NUMERIC),
        // SqlParameter("p_hold_until_date", Types.DATE),
        SqlParameter("p_txn_entry_amount", Types.NUMERIC),
        SqlParameter("p_client_unique_ref", Types.VARCHAR),
        // SqlOutParameter("p_txn_id", Types.NUMERIC),
        // SqlOutParameter("p_txn_entry_seq", Types.NUMERIC),
        SqlOutParameter("p_hold_number", Types.NUMERIC),
      )
    compile()
  }
}

@Component
class RemoveFinanceHold(dataSource: DataSource) : SimpleJdbcCall(dataSource) {
  init {
    withSchemaName(StoreProcMetadata.API_OWNER)
      .withCatalogName(StoreProcMetadata.API_FINANCE_PROCS)
      .withProcedureName("release_hold")
      .withoutProcedureColumnMetaDataAccess()
      .withNamedBinding()
      .declareParameters(
        SqlParameter("p_noms_id", Types.VARCHAR),
        SqlParameter("p_root_offender_id", Types.NUMERIC),
        SqlParameter("p_single_offender_id", Types.NUMERIC),
        SqlParameter("p_agy_loc_id", Types.VARCHAR),
        SqlParameter("p_txn_entry_desc", Types.VARCHAR),
        SqlParameter("p_hold_number", Types.NUMERIC),
      )
    compile()
  }
}
