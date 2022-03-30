package uk.gov.justice.hmpps.prison.repository.storedprocs;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.SimpleJdbcCallWithExceptionTranslater;

import javax.sql.DataSource;
import java.sql.Types;

@Component
public class WorkflowNotificationProcs {

    @Component
    public static class CreateWorkflowTask extends SimpleJdbcCallWithExceptionTranslater {
        public CreateWorkflowTask(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName("OMS_OWNER")
                    .withCatalogName("OIDADMIS")
                    .withProcedureName("create_workflow_task")
                    .withoutProcedureColumnMetaDataAccess()
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter("p_offender_book_id", Types.NUMERIC),
                            new SqlParameter("p_movement_rsn_code", Types.VARCHAR),
                            new SqlParameter("p_movement_date", Types.DATE),
                            new SqlParameter("p_movement_time", Types.TIME),
                            new SqlParameter("p_old_caseload_id", Types.NUMERIC),
                            new SqlParameter("p_new_caseload_id", Types.NUMERIC),
                            new SqlParameter("p_team_id", Types.NUMERIC)
                    );
            compile();
        }
    }
}
