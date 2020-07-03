package uk.gov.justice.hmpps.prison.repository.v1.storedprocs;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import uk.gov.justice.hmpps.prison.repository.v1.model.AvailableDatesSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.ContactPersonSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.UnavailabilityReasonSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.VisitSlotsSP;

import javax.sql.DataSource;
import java.sql.Types;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.API_OWNER;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.API_VISIT_PROCS;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ADULT_CNT;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CONTACT_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_DATES;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_DATE_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_FROM_DATE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_REASON_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROOT_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TO_DATE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_VISITOR_CNT;

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

    @Component
    public static class GetVisitSlotsWithCapacity extends SimpleJdbcCallWithExceptionTranslater {

        public GetVisitSlotsWithCapacity(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_VISIT_PROCS)
                    .withProcedureName("prison_visit_slotswithcapacity")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlParameter(P_FROM_DATE, Types.DATE),
                            new SqlParameter(P_TO_DATE, Types.DATE),
                            new SqlParameter(P_VISITOR_CNT, Types.INTEGER),
                            new SqlParameter(P_ADULT_CNT, Types.INTEGER),
                            new SqlOutParameter(P_DATE_CSR, Types.REF_CURSOR))
                    .returningResultSet(P_DATE_CSR,
                            StandardBeanPropertyRowMapper.newInstance(VisitSlotsSP.class));
            compile();
        }
    }
}
