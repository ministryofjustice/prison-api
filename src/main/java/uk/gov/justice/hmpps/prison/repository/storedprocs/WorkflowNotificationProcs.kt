package uk.gov.justice.hmpps.prison.repository.storedprocs

import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.SimpleJdbcCallWithExceptionTranslater
import org.springframework.jdbc.core.SqlParameter
import org.springframework.stereotype.Component
import java.sql.Types
import javax.sql.DataSource

@Component
class WorkflowNotificationProcs {
    @Component
    class CreateWorkflowTask(dataSource: DataSource?, errorCodeTranslator: NomisV1SQLErrorCodeTranslator?) :
        SimpleJdbcCallWithExceptionTranslater(dataSource, errorCodeTranslator) {
        init {
            withSchemaName("OMS_OWNER")
                .withCatalogName("OIDADMIS")
                .withProcedureName("create_workflow_task")
                .withoutProcedureColumnMetaDataAccess()
                .withNamedBinding()
                .declareParameters(
                    SqlParameter("p_offender_book_id", Types.NUMERIC),
                    SqlParameter("p_movement_rsn_code", Types.VARCHAR),
                    SqlParameter("p_movement_date", Types.DATE),
                    SqlParameter("p_movement_time", Types.TIME),
                    SqlParameter("p_old_caseload_id", Types.NUMERIC),
                    SqlParameter("p_new_caseload_id", Types.NUMERIC),
                    SqlParameter("p_team_id", Types.NUMERIC)
                )
            compile()
        }
    }
}