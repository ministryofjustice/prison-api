package uk.gov.justice.hmpps.prison.util;

import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;

import java.util.Map;
import java.util.Optional;

public class H2QueryBuilder extends AbstractQueryBuilder {

    private static final String COUNT_SELECT = "WITH TOTAL_COUNT AS ( SELECT COUNT(*) AS RECORD_COUNT %s ) SELECT * FROM TOTAL_COUNT, (";

    public H2QueryBuilder(final String initialSQL, final Map<String, FieldMapper> fieldMap, final DatabaseDialect dialect) {
        super(initialSQL, fieldMap, dialect);
    }

    public String build() {
        var result = new StringBuilder();
        final var statementType = getStatementType();

        if (Optional.of(SQLKeyword.SELECT).equals(statementType)) {
            // Prepare initial SQL - wrap and apply additional criteria, as necessary.
            final var preparedSql = prepareSql();

            result.append(preparedSql);

            // Apply any sorting...
            final var strOrderBy = (StringUtils.isBlank(extraOrderBy)) ? " " : " " + (SQLKeyword.ORDER_BY + " " + extraOrderBy);

            result.append(strOrderBy);

            // Append pagination support...
            if (includePagination) {
                buildPaginationSql(result);
            }

            // Apply record count based on full criteria defined in prepared SQL...
            if (includeRowCount || includeDirectRowCount) {
                buildAnsiDataCountSql(result, preparedSql);
            }

            // Remove special characters if required...
            if (removeSpecialChars) {
                result = new StringBuilder(removeSpecialCharacters(result.toString()));
            }

            // Replace all occurrences of 'WM_CONCAT' keyword with 'GROUP_CONCAT'...
            result = new StringBuilder(StringUtils.replaceAll(result.toString(), "WM_CONCAT", "GROUP_CONCAT"));
        } else {
            result.append(initialSQL);
        }

        return result.toString();
    }

    private void buildPaginationSql(final StringBuilder result) {
        result.append(" OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
    }

    private void buildAnsiDataCountSql(final StringBuilder result, final String fullSql) {
        final var criteria = QueryUtil.getCriteriaFromQuery(fullSql);

        result.insert(0, String.format(COUNT_SELECT, criteria));

        if (includePagination) {
            result.append(")");
        }
    }

    private String prepareSql() {
        final var preparedSql = new StringBuilder();

        if (includeRowCount || extraWhere.length() > 0 || extraOrderBy.length() > 0) {
            preparedSql.append("SELECT QRY_ALIAS.* FROM (").append(initialSQL).append(") QRY_ALIAS");

            if (extraWhere.length() > 0) {
                preparedSql.append(" WHERE ").append(extraWhere);
            }
        } else {
            preparedSql.append(initialSQL);
        }

        return preparedSql.toString();
    }
}
