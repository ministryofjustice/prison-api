package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.NomisV1SQLErrorCodeTranslator;
import net.syscon.elite.repository.v1.model.AvailableDatesSP;
import net.syscon.elite.repository.v1.model.ContactPersonSP;
import net.syscon.elite.repository.v1.model.UnavailabilityReasonSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

public class VisitsProc {

    @Component
    public static class GetAvailableDates extends SimpleJdbcCallWithExceptionTranslater {
        public GetAvailableDates(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_VISIT_PROCS)
                    .withProcedureName("offender_available_dates")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_FROM_DATE, Types.DATE),
                            new SqlParameter(P_TO_DATE, Types.DATE))
                    .returningResultSet(P_DATE_CSR,
                            StandardBeanPropertyRowMapper.newInstance(AvailableDatesSP.class));
            compile();
        }
    }

    @Component
    public static class GetContactList extends SimpleJdbcCallWithExceptionTranslater {
        public GetContactList(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_VISIT_PROCS)
                    .withProcedureName("get_offender_contacts")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.VARCHAR))
                    .returningResultSet(P_CONTACT_CSR, StandardBeanPropertyRowMapper.newInstance(ContactPersonSP.class));
            compile();
        }
    }

    @Component
    public static class GetUnavailability extends SimpleJdbcCallWithExceptionTranslater {

        public GetUnavailability(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_VISIT_PROCS)
                    .withProcedureName("offender_unavailable_reasons")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_ROOT_OFFENDER_ID, Types.VARCHAR),
                            new SqlParameter(P_DATES, Types.VARCHAR),
                            new SqlOutParameter(P_REASON_CSR, Types.REF_CURSOR))
                    .returningResultSet(P_REASON_CSR,
                            StandardBeanPropertyRowMapper.newInstance(UnavailabilityReasonSP.class));
            compile();
        }
    }
}
