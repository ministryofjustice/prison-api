package uk.gov.justice.hmpps.prison.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;

import java.util.Map;

/**
 * Constructs an appropriate Query Builder implementation based on initial SQL provided.
 *
 * @author Andrew Knight
 */
@Component
public class QueryBuilderFactory {

    private final DatabaseDialect dialect;

    @Autowired
    public QueryBuilderFactory(@Value("${schema.database.dialect}") final DatabaseDialect dialect) {
        this.dialect = dialect;
    }

    public <T> IQueryBuilder getQueryBuilder(final String initialSql, final StandardBeanPropertyRowMapper<T> rowMapper) {
        return getQueryBuilder(initialSql, rowMapper.getFieldMap());
    }

    public IQueryBuilder getQueryBuilder(final String initialSql, final Map<String, FieldMapper> fieldMap) {
        final IQueryBuilder queryBuilder;

        switch (dialect) {
            case HSQLDB:
                queryBuilder = new HSQLDBQueryBuilder(initialSql, fieldMap, dialect);

                break;

            case ORACLE_11:
            case ORACLE_12:
            case POSTGRES:
                queryBuilder = new OracleQueryBuilder(initialSql, fieldMap, dialect);

                break;

            default:
                queryBuilder = null;

                break;
        }

        return queryBuilder;
    }
}
