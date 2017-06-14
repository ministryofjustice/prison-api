package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by andrewk on 13/06/2017.
 */
public abstract class AbstractQueryBuilder implements IQueryBuilder {
    protected final String initialSQL;
    protected final Map<String, FieldMapper> fieldMap;

    protected boolean includePagination;
    protected boolean includeRowCount;

    protected final StringBuilder extraWhere = new StringBuilder();

    protected String extraOrderBy = "";

    protected AbstractQueryBuilder(String initialSQL, Map<String, FieldMapper> fieldMap) {
        this.initialSQL = initialSQL;
        this.fieldMap = fieldMap;
    }

    public IQueryBuilder addPagination() {
        includePagination = true;

        return this;
    }

    public IQueryBuilder addRowCount() {
        includeRowCount = true;

        return this;
    }

    public IQueryBuilder addQuery(final String query) {
        if (StringUtils.isNotBlank(query)) {
            List<String> queryList = QueryUtil.checkPrecdencyAndSplit(query, new ArrayList<>());

            queryList.stream()
                    .filter(StringUtils::isNotBlank)
                    .forEach(queryItem -> {
                        if (queryItem.contains("(") && queryItem.contains(")")) {
                            String modifiedQueryItem = queryItem
                                    .replace("(", "")
                                    .replace(")", "");

                            extraWhere.append(
                                    QueryUtil.prepareQuery(modifiedQueryItem, true, fieldMap));
                        } else {
                            extraWhere.append(
                                    QueryUtil.prepareQuery(queryItem, false, fieldMap));
                        }
                    });
        }

        return this;
    }

    public IQueryBuilder addOrderBy(boolean isAscending, String... fields) {
        String key = fieldMap.entrySet().stream()
                .filter(entry -> entry.getValue().getName().equals(fields[0]))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);

        if (key != null) {
            extraOrderBy += (key + " " + (isAscending ? "" : SQLKeyword.DESC));
        }

        return this;
    }

    protected Optional<SQLKeyword> getStatementType() {
        String modifiedSQL = initialSQL.toUpperCase()
                .replace('\t', ' ')
                .replace('\r', ' ');

        String[] lines = modifiedSQL.split("\\n");

        SQLKeyword statementType = null;

        for (String line : lines) {
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
}
