package net.syscon.util;

import net.syscon.prison.api.support.Order;
import net.syscon.prison.api.support.PageRequest;
import net.syscon.prison.repository.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.syscon.util.QueryUtil.prepareQuery;

public abstract class AbstractQueryBuilder implements IQueryBuilder {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    protected final String initialSQL;
    protected final Map<String, FieldMapper> fieldMap;
    protected final Map<String, String> fieldNameToColumnMap;

    protected boolean includePagination;
    protected boolean includeRowCount;
    protected boolean includeDirectRowCount;

    protected final StringBuilder extraWhere = new StringBuilder();

    protected String extraOrderBy = "";
    protected DatabaseDialect dialect;
    protected boolean removeSpecialChars;

    protected AbstractQueryBuilder(final String initialSQL, final Map<String, FieldMapper> fieldMap, final DatabaseDialect dialect) {
        this.initialSQL = initialSQL;
        this.dialect = dialect;
        this.fieldMap = fieldMap;
        if (fieldMap != null) {
            this.fieldNameToColumnMap = fieldMap.entrySet().stream()
                    .collect(Collectors.toMap(v -> v.getValue().getName(),
                            Map.Entry::getKey));
        } else {
            this.fieldNameToColumnMap = Collections.emptyMap();
        }
    }

    @Override
    public DatabaseDialect getDialect() {
        return this.dialect;
    }

    @Override
    public IQueryBuilder removeSpecialChars() {
        this.removeSpecialChars = true;
        return this;
    }

    @Override
    public IQueryBuilder addPagination() {
        includePagination = true;

        return this;
    }

    @Override
    public IQueryBuilder addRowCount() {
        includeRowCount = true;

        return this;
    }

    @Override
    public IQueryBuilder addDirectRowCount() {
        includeDirectRowCount = true;

        return this;
    }

    @Override
    public IQueryBuilder addQuery(final String query) {
        if (StringUtils.isNotBlank(query)) {
            final List<String> queryTerms = new ArrayList<>();

            final var queryList = QueryUtil.checkPrecedencyAndSplit(query, new ArrayList<>());

            queryList.stream()
                    .filter(StringUtils::isNotBlank)
                    .forEach(queryItem -> {
                        if (queryItem.contains("(") && queryItem.contains(")")) {
                            final var modifiedQueryItem = queryItem
                                    .replace("(", "")
                                    .replace(")", "");

                            queryTerms.add(prepareQuery(modifiedQueryItem, true, fieldMap));
                        } else {
                            queryTerms.add(prepareQuery(queryItem, false, fieldMap));
                        }
                    });

            final var fullQuery = StringUtils.normalizeSpace(StringUtils.join(queryTerms, " "));

            if (fullQuery.startsWith("AND ")) {
                extraWhere.append(fullQuery.substring(4));
            } else if (fullQuery.startsWith("OR ")) {
                extraWhere.append(fullQuery.substring(3));
            }
        }

        return this;
    }

    @Override
    public IQueryBuilder addWhereClause(final String whereClause) {
        if (StringUtils.isNotBlank(whereClause)) {

            if (whereClause.startsWith("AND ")) {
                extraWhere.append(whereClause.substring(4));
            } else if (whereClause.startsWith("OR ")) {
                extraWhere.append(whereClause.substring(3));
            } else {
                extraWhere.append(whereClause);
            }
        }

        return this;
    }

    @Override
    public IQueryBuilder addOrderBy(final boolean isAscending, final String fields) {

        if (extraOrderBy.length() > 0) {
            extraOrderBy += ", ";
        }
        extraOrderBy += COMMA_PATTERN
                .splitAsStream(fields == null ? "" : fields)
                .map(fieldNameToColumnMap::get)
                .filter(Objects::nonNull)
                .map(s -> s + " " + addOrderDirection(isAscending))
                .collect(Collectors.joining(", "));

        return this;
    }

    @Override
    public IQueryBuilder addOrderBy(final Order order, final String fields) {
        return addOrderBy(Order.ASC == order, fields);
    }

    @Override
    public IQueryBuilder addOrderBy(final PageRequest pageRequest) {
        return addOrderBy(pageRequest.isAscendingOrder(), pageRequest.getOrderBy());
    }

    private SQLKeyword addOrderDirection(final boolean isAscending) {
        return isAscending ? SQLKeyword.ASC : SQLKeyword.DESC;
    }

    protected String removeSpecialCharacters(final String sql) {
        if (sql == null) return null;
        final String stmts[] = {sql, sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ')};
        while (!stmts[0].equals(stmts[1])) {
            stmts[0] = stmts[1];
            stmts[1] = stmts[1].replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
            stmts[1] = stmts[1].replaceAll("  ", " ");
        }
        return stmts[0].trim();
    }

    protected Optional<SQLKeyword> getStatementType() {
        final var modifiedSQL = initialSQL.toUpperCase()
                .replace('\t', ' ')
                .replace('\r', ' ');

        final var lines = modifiedSQL.split("\\n");

        SQLKeyword statementType = null;

        for (final var line : lines) {
            if (StringUtils.startsWithIgnoreCase(line.trim(), SQLKeyword.SELECT.toString())) {
                statementType = SQLKeyword.SELECT;

                break;
            }

            if (StringUtils.startsWithIgnoreCase(line.trim(), SQLKeyword.INSERT.toString())) {
                statementType = SQLKeyword.INSERT;

                break;
            }

            if (StringUtils.startsWithIgnoreCase(line.trim(), SQLKeyword.UPDATE.toString())) {
                statementType = SQLKeyword.UPDATE;

                break;
            }

            if (StringUtils.startsWithIgnoreCase(line.trim(), SQLKeyword.DELETE.toString())) {
                statementType = SQLKeyword.DELETE;

                break;
            }
        }

        return Optional.ofNullable(statementType);
    }

    @Override
    public abstract String build();
}
