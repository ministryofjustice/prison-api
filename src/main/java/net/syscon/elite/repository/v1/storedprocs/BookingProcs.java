package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.model.BookingSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

public class BookingProcs {

    private static final String API_BOOKING_PROCS = "api_booking_procs";

    @Component
    public static class GetLatestBooking extends SimpleJdbcCall {

        public GetLatestBooking(DataSource dataSource) {
            super(dataSource);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_BOOKING_PROCS)
                    .withProcedureName("get_latest_booking")
                    .withNamedBinding()
                .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlOutParameter(StoreProcMetadata.P_BOOKING_CSR, Types.REF_CURSOR)
                )
                .returningResultSet(StoreProcMetadata.P_BOOKING_CSR,
                        StandardBeanPropertyRowMapper.newInstance(BookingSP.class));
            compile();
        }
    }

    @Component
    public static class GetOffenderBookings extends SimpleJdbcCall {

        public GetOffenderBookings(DataSource dataSource) {
            super(dataSource);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_BOOKING_PROCS)
                    .withProcedureName("get_offender_bookings")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlOutParameter(StoreProcMetadata.P_BOOKING_CSR, Types.REF_CURSOR))
                    .returningResultSet(StoreProcMetadata.P_BOOKING_CSR,
                            StandardBeanPropertyRowMapper.newInstance(BookingSP.class));
            compile();
        }
    }

}
