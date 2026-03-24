package uk.gov.justice.hmpps.prison.repository.storedprocs

import org.springframework.jdbc.core.SqlInOutParameter
import org.springframework.jdbc.core.SqlParameter
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.SimpleJdbcCallWithExceptionTranslater
import java.sql.Types
import javax.sql.DataSource

@Component
class TrustProcs {
  @Component
  class InsertIntoOffenderTrans(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName("OMS_OWNER")
        .withCatalogName("TRUST")
        .withProcedureName("insert_into_offender_trans")
        .withoutProcedureColumnMetaDataAccess()
        .withNamedBinding()
        .declareParameters(
          SqlParameter("p_trans_number", Types.NUMERIC),
          SqlParameter("p_trans_seq", Types.NUMERIC),
          SqlParameter("p_csld_id", Types.VARCHAR),
          SqlParameter("p_off_id", Types.NUMERIC),
          SqlParameter("p_off_book_id", Types.NUMERIC),
          SqlParameter("p_trans_post_type", Types.VARCHAR),
          SqlParameter("p_trans_type", Types.VARCHAR),
          SqlParameter("p_trans_desc", Types.VARCHAR),
          SqlParameter("p_trans_amount", Types.NUMERIC),
          SqlParameter("p_trans_date", Types.DATE),
          SqlParameter("p_sub_act_type", Types.VARCHAR),
          SqlParameter("p_deduction_flag", Types.VARCHAR),
          SqlParameter("p_pre_ded_amount", Types.NUMERIC),
          SqlParameter("p_deduction_type", Types.VARCHAR),
          SqlParameter("p_payee_corp_id", Types.NUMERIC),
          SqlParameter("p_payee_person_id", Types.NUMERIC),
          SqlParameter("p_info_number", Types.VARCHAR),
          SqlParameter("p_slip_print_flag", Types.VARCHAR),
          SqlParameter("p_allow_overdrawn", Types.VARCHAR),
        )
      compile()
    }
  }

  @Component
  class ProcessGlTransNew(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName("OMS_OWNER")
        .withCatalogName("TRUST")
        .withProcedureName("process_gl_trans_new")
        .withoutProcedureColumnMetaDataAccess()
        .withNamedBinding()
        .declareParameters(
          SqlParameter("p_csld_id", Types.VARCHAR),
          SqlParameter("p_trans_type", Types.VARCHAR),
          SqlParameter("p_operation_type", Types.VARCHAR),
          SqlParameter("p_trans_amount", Types.NUMERIC),
          SqlParameter("p_trans_number", Types.NUMERIC),
          SqlParameter("p_trans_date", Types.DATE),
          SqlParameter("p_trans_desc", Types.VARCHAR),
          SqlParameter("p_trans_seq", Types.NUMERIC),
          SqlParameter("p_module_name", Types.VARCHAR),
          SqlParameter("p_off_id", Types.NUMERIC),
          SqlParameter("p_off_book_id", Types.NUMERIC),
          SqlParameter("p_sub_act_type_dr", Types.VARCHAR),
          SqlParameter("p_sub_act_type_cr", Types.VARCHAR),
          SqlParameter("p_payee_pers_id", Types.NUMERIC),
          SqlParameter("p_payee_corp_id", Types.NUMERIC),
          SqlParameter("p_payee_name_text", Types.VARCHAR),
          SqlInOutParameter("p_gl_sqnc", Types.NUMERIC),
          SqlParameter("p_off_ded_id", Types.NUMERIC),
        )
      compile()
    }
  }

  @Component
  class UpdateOffenderBalance(dataSource: DataSource, errorCodeTranslator: NomisV1SQLErrorCodeTranslator) : SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName("OMS_OWNER")
        .withCatalogName("TRUST")
        .withProcedureName("update_offender_balance")
        .withoutProcedureColumnMetaDataAccess()
        .withNamedBinding()
        .declareParameters(
          SqlParameter("p_csld_id", Types.VARCHAR),
          SqlParameter("p_off_id", Types.NUMERIC),
          SqlParameter("p_trans_post_type", Types.VARCHAR),
          SqlParameter("p_trans_date", Types.DATE),
          SqlParameter("p_trans_number", Types.NUMERIC),
          SqlParameter("p_trans_type", Types.VARCHAR),
          SqlParameter("p_trans_amount", Types.NUMERIC),
          SqlParameter("p_sub_act_type", Types.VARCHAR),
          SqlParameter("p_allow_overdrawn", Types.VARCHAR),
        )
      compile()
    }
  }
}
