package net.syscon.prison.repository.v1.storedprocs;

import net.syscon.prison.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import net.syscon.prison.repository.v1.model.LiveRollSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.API_OWNER;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROLL_CSR;

public class PrisonProcs {

    private static final String API_PRISON_PROCS = "api_prison_procs";

    @Component
    public static class GetLiveRoll extends SimpleJdbcCallWithExceptionTranslater {

        public GetLiveRoll(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_PRISON_PROCS)
                    .withProcedureName("get_prison_roll")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlOutParameter(P_ROLL_CSR, Types.REF_CURSOR)
                    )
                    .returningResultSet(P_ROLL_CSR,
                            StandardBeanPropertyRowMapper.newInstance(LiveRollSP.class));
            compile();
        }
    }
}
