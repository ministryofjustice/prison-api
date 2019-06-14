package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.model.LatestBookingSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

@Component
public class GetLatestBookingProc extends SimpleJdbcCall {

    private static final String API_BOOKING_PROCS = "api_booking_procs";

    public GetLatestBookingProc(final DataSource dataSource) {
        super(dataSource);
        this
            .withSchemaName(StoreProcMetadata.API_OWNER)
            .withCatalogName(API_BOOKING_PROCS)
            .withProcedureName("get_latest_booking")
            .declareParameters(
                    new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                    new SqlOutParameter(StoreProcMetadata.P_BOOKING_CSR, Types.REF_CURSOR))
            .returningResultSet(StoreProcMetadata.P_BOOKING_CSR,
                    StandardBeanPropertyRowMapper.newInstance(LatestBookingSP.class));
        compile();
    }


}
