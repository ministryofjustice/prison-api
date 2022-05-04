package uk.gov.justice.hmpps.prison.repository.storedprocs;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.SimpleJdbcCallWithExceptionTranslater;

import javax.sql.DataSource;
import java.sql.Types;

@Component
public class OffenderAdminProcs {

    @Component
    public static class CreateTrustAccount extends SimpleJdbcCallWithExceptionTranslater {
        public CreateTrustAccount(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName("OMS_OWNER")
                    .withCatalogName("OIDADMIS")
                    .withProcedureName("create_trust_account")
                    .withoutProcedureColumnMetaDataAccess()
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter("p_caseload_id", Types.VARCHAR),
                            new SqlParameter("p_off_book_id", Types.NUMERIC),
                            new SqlParameter("p_root_off_id", Types.NUMERIC),
                            new SqlParameter("p_from_agy_loc_id", Types.VARCHAR),
                            new SqlParameter("p_mvmt_reason_code", Types.VARCHAR),
                            new SqlParameter("p_shadow_id", Types.NUMERIC),
                            new SqlParameter("p_receipt_no", Types.NUMERIC),
                            new SqlParameter("p_dest_caseload_id", Types.VARCHAR)
                    );
            compile();
        }
    }

    @Component
    public static class GenerateNewBookingNo extends SimpleJdbcCallWithExceptionTranslater {
        public GenerateNewBookingNo(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName("OMS_OWNER")
                .withCatalogName("OIDADMIS")
                .withFunctionName("generate_new_booking_no");
            compile();
        }
    }

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
                    new SqlParameter("p_movement_time", Types.VARCHAR),
                    new SqlParameter("p_old_caseload_id", Types.VARCHAR),
                    new SqlParameter("p_new_caseload_id", Types.VARCHAR),
                    new SqlParameter("p_team_id", Types.NUMERIC)
                );
            compile();
        }
    }

}
