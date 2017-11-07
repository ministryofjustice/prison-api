package net.syscon.util;

import net.syscon.elite.repository.mapping.FieldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    public QueryBuilderFactory(@Value("${schema.database.dialect}") DatabaseDialect dialect) {
        this.dialect = dialect;
    };

    public IQueryBuilder getQueryBuilder(String initialSql, Map<String, FieldMapper> fieldMap) {
        IQueryBuilder queryBuilder = null;

        switch(dialect) {
            case HSQLDB:
                queryBuilder = new HSQLDBQueryBuilder(initialSql, fieldMap, dialect);
                break;
            case ORACLE_11:
            case ORACLE_12:
            case POSTGRES:
                queryBuilder = new OracleQueryBuilder(initialSql, fieldMap, dialect);
                break;
        }
        return queryBuilder;
    }
}
