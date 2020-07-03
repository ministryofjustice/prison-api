package uk.gov.justice.hmpps.prison.repository.v1.storedprocs;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator;

import javax.sql.DataSource;
import java.sql.Types;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.API_CORE_PROCS;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.API_OWNER;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_BIRTH_DATE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_NUMBER;

public class CoreProcs {

    @Component
    public static class GetActiveOffender extends SimpleJdbcCallWithExceptionTranslater {

        public GetActiveOffender(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_CORE_PROCS)
                    .withFunctionName("get_active_offender_id")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_NUMBER, Types.VARCHAR),
                            new SqlParameter(P_BIRTH_DATE, Types.DATE));
            compile();
        }
    }
}
