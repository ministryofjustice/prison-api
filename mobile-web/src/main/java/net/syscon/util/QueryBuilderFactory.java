package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Constructs an appropriate Query Builder implementation based on initial SQL provided.
 *
 * @author Andrew Knight
 */
@Component
public class QueryBuilderFactory {

    public static IQueryBuilder getQueryBuilder(String initialSql, Map<String, FieldMapper> fieldMap) {
        IQueryBuilder queryBuilder;

        // TODO: Consider controlling this using a database type property
        if (StringUtils.containsAny(initialSql,
                IQueryBuilder.SQL_PLACEHOLDER_WHERE_QUERY,
                IQueryBuilder.SQL_PLACEHOLDER_AND_QUERY,
                IQueryBuilder.SQL_PLACEHOLDER_OR_QUERY)) {
            queryBuilder = new StandardQueryBuilder(initialSql, fieldMap);
        } else {
            queryBuilder = new OracleQueryBuilder(initialSql, fieldMap);
        }

        return queryBuilder;
    }
}
