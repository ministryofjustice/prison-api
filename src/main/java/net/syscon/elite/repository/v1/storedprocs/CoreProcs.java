package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.v1.NomisV1SQLErrorCodeTranslator;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

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
