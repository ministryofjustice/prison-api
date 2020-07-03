package net.syscon.prison.repository.v1.storedprocs;

import net.syscon.prison.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import net.syscon.prison.repository.v1.model.EventSP;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_EVENTS_CSR;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROOT_OFFENDER_ID;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_SINGLE_OFFENDER_ID;

@Component
public class EventProcs {

    public static final String P_EVENT_TYPE = "p_event_type";
    public static final String P_FROM_TS = "p_from_ts";
    public static final String P_LIMIT = "p_limit";

    private static final String API_OFFENDER_EVENT_PROCS = "api_offender_event";

    @Component
    public static class GetEvents extends SimpleJdbcCallWithExceptionTranslater {
        public GetEvents(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_OFFENDER_EVENT_PROCS)
                    .withProcedureName("get_pss_events")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_EVENT_TYPE, Types.VARCHAR),
                            new SqlParameter(P_FROM_TS, Types.TIMESTAMP),
                            new SqlParameter(P_LIMIT, Types.INTEGER))
                    .returningResultSet(P_EVENTS_CSR,
                            StandardBeanPropertyRowMapper.newInstance(EventSP.class));
            compile();
        }
    }
}
