package uk.gov.justice.hmpps.prison.repository.storedprocs;

import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.SimpleJdbcCallWithExceptionTranslater;

import javax.sql.DataSource;
import java.sql.Types;

@Component
public class CopyProcs {

    @Component
    public static class CopyBookData extends SimpleJdbcCallWithExceptionTranslater {
        public CopyBookData(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName("OMS_OWNER")
                    .withCatalogName("OMKCOPY")
                    .withProcedureName("COPY_BOOK_DATA")
                    .withoutProcedureColumnMetaDataAccess()
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter("p_move_type", Types.VARCHAR),
                            new SqlParameter("p_move_reason", Types.VARCHAR),
                            new SqlParameter("p_old_book_id", Types.NUMERIC),
                            new SqlParameter("p_new_book_id", Types.NUMERIC),
                            new SqlInOutParameter("p_return_text", Types.VARCHAR),
                            new SqlInOutParameter("v_parent", Types.VARCHAR));
            compile();
        }
    }

}
