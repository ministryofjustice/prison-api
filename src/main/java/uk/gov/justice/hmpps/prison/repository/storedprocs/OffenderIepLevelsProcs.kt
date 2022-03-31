package uk.gov.justice.hmpps.prison.repository.storedprocs

import org.springframework.jdbc.core.SqlParameter
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.SimpleJdbcCallWithExceptionTranslater
import java.sql.Types
import javax.sql.DataSource

@Component
class OffenderIepLevelsProcs {
  @Component
  class CreateOffenderIepLevels(dataSource: DataSource?, errorCodeTranslator: NomisV1SQLErrorCodeTranslator?) :
    SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
    init {
      withSchemaName("OMS_OWNER")
        .withCatalogName("OIDADMIS")
        .withProcedureName("create_offender_iep_levels")
        .withoutProcedureColumnMetaDataAccess()
        .withNamedBinding()
        .declareParameters(
          SqlParameter("p_off_book_id", Types.NUMERIC),
          SqlParameter("p_to_agy_loc_id", Types.NUMERIC),
          SqlParameter("p_movement_date", Types.DATE)
        )
      compile()
    }
  }
}
