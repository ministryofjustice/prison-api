package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.model.AlertSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

public class AlertsProcs {

    public static final String P_ALERTS_CSR       = "p_alerts_csr";
    public static final String ALERT_SEQ = "alert_seq";
    public static final String ALERT_TYPE = "alert_type";
    public static final String ALERT_TYPE_DESC = "alert_type_desc";
    public static final String ALERT_CODE = "alert_code";
    public static final String ALERT_CODE_DESC = "alert_code_desc";
    public static final String ALERT_DATE = "alert_date";
    public static final String EXPIRY_DATE = "expiry_date";
    public static final String ALERT_STATUS = "alert_status";
    public static final String COMMENT_TEXT = "comment_text";

    private static final String API_ALERTS_PROCS = "api_alerts";
    public static final String P_INCLUDE_INACTIVE = "p_include_inactive";
    public static final String P_MODIFIED_SINCE = "p_modified_since";

    @Component
    public static class GetAlerts extends SimpleJdbcCall {

        public GetAlerts(DataSource dataSource) {
            super(dataSource);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_ALERTS_PROCS)
                    .withProcedureName("get_alerts")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_INCLUDE_INACTIVE, Types.VARCHAR),
                            new SqlParameter(P_MODIFIED_SINCE, Types.TIMESTAMP),
                            new SqlOutParameter(P_ALERTS_CSR, Types.REF_CURSOR))
                    .returningResultSet(P_ALERTS_CSR,
                            StandardBeanPropertyRowMapper.newInstance(AlertSP.class));
            compile();
        }
    }

}
