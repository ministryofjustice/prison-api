package net.syscon.prison.repository.v1.storedprocs;

import net.syscon.prison.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import net.syscon.prison.repository.v1.model.ChargeSP;
import net.syscon.prison.repository.v1.model.LegalCaseSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.API_LEGAL_PROCS;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.API_OWNER;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_CASES_CSR;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_CASE_ID;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_CHARGES_CSR;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_OFFENDER_BOOK_ID;

public class LegalProcs {

    @Component
    public static class GetBookingCases extends SimpleJdbcCallWithExceptionTranslater {

        public GetBookingCases(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
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
    public static class GetCaseCharges extends SimpleJdbcCallWithExceptionTranslater {

        public GetCaseCharges(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
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
