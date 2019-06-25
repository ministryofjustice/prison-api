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

public class LegalProcs {

    private static final String API_LEGAL_PROCS = "api_legal_procs";
    public static final String P_CASES_CSR = "p_cases_csr";
    public static final String P_CHARGES_CSR = "p_charges_csr";
    public static final String P_CASE_ID = "p_case_id";

    @Component
    public static class GetBookingCases extends SimpleJdbcCall {

        public GetBookingCases(DataSource dataSource) {
            super(dataSource);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_LEGAL_PROCS)
                    .withProcedureName("get_booking_cases")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_OFFENDER_BOOK_ID, Types.INTEGER),
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
            withSchemaName(StoreProcMetadata.API_OWNER)
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
