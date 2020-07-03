package uk.gov.justice.hmpps.prison.repository.v1.storedprocs;

import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator;

import javax.sql.DataSource;

public class SimpleJdbcCallWithExceptionTranslater extends SimpleJdbcCall {
    public SimpleJdbcCallWithExceptionTranslater(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
        super(dataSource);
        getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
    }
}
