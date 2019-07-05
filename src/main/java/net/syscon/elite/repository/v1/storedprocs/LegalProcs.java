package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.model.ChargeSP;
import net.syscon.elite.repository.v1.model.LegalCaseSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

public class LegalProcs {

    @Component
    public static class GetBookingCases extends SimpleJdbcCall {

        public GetBookingCases(DataSource dataSource) {
            super(dataSource);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_LEGAL_PROCS)
                    .withProcedureName("get_booking_cases")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_OFFENDER_BOOK_ID, Types.INTEGER),
                            new SqlOutParameter(P_CASES_CSR, Types.REF_CURSOR))
                    .returningResultSet(P_CASES_CSR,
                            StandardBeanPropertyRowMapper.newInstance(LegalCaseSP.class));
            compile();
        }
    }

    @Component
    public static class GetCaseCharges extends SimpleJdbcCall {

        public GetCaseCharges(DataSource dataSource) {
            super(dataSource);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_LEGAL_PROCS)
                    .withProcedureName("get_case_charges")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_CASE_ID, Types.INTEGER),
                            new SqlOutParameter(P_CHARGES_CSR, Types.REF_CURSOR))
                    .returningResultSet(P_CHARGES_CSR,
                            StandardBeanPropertyRowMapper.newInstance(ChargeSP.class));
            compile();
        }
    }

}
