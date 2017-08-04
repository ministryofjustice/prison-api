package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class StandardQueryBuilder extends AbstractQueryBuilder {

	public StandardQueryBuilder(final String initialSQL, final Map<String, FieldMapper> fieldMap, DatabaseDialect dialect) {
		super(initialSQL, fieldMap, dialect);

		hasAndQueryPlaceholder = initialSQL.contains(SQL_PLACEHOLDER_AND_QUERY);
		hasOrQueryPlaceholder = initialSQL.contains(SQL_PLACEHOLDER_OR_QUERY);
		hasWhereQueryPlaceholder = initialSQL.contains(SQL_PLACEHOLDER_WHERE_QUERY);
		hasOrderByPlaceholder = initialSQL.contains(SQL_PLACEHOLDER_ORDER_BY);
	}

	private final boolean hasAndQueryPlaceholder;
	private final boolean hasOrQueryPlaceholder;
	private final boolean hasWhereQueryPlaceholder;
	private final boolean hasOrderByPlaceholder;

	public StandardQueryBuilder addRowCount() {
		// With standard query builder, row count, when required, is expected to be part of initial SQL
		includeRowCount = false;

		return this;
	}

	public StandardQueryBuilder addPagination() {
		// With standard query builder, pagination, when required, is expected to be part of initial SQL
		includePagination = false;

		return this;
	}

	public String build() {
		String parsedQuery = initialSQL;
		Optional<SQLKeyword> statementType = getStatementType();

		if (Optional.of(SQLKeyword.SELECT).equals(statementType)) {
			boolean haveQuery = StringUtils.isNotBlank(extraWhere);

			if (hasWhereQueryPlaceholder) {
				String replacement = (haveQuery ? SQLKeyword.WHERE + " " + extraWhere : "");

				parsedQuery = parsedQuery.replaceAll(SQL_PLACEHOLDER_WHERE_QUERY, replacement);
			}

			if (hasAndQueryPlaceholder) {
				String replacement = (haveQuery ? SQLKeyword.AND + " " + extraWhere : "");

				parsedQuery = parsedQuery.replaceAll(SQL_PLACEHOLDER_AND_QUERY, replacement);
			}

			if (hasOrQueryPlaceholder) {
				String replacement = (haveQuery ? SQLKeyword.WHERE + " " + extraWhere : "");

				parsedQuery = parsedQuery.replaceAll(SQL_PLACEHOLDER_OR_QUERY, replacement);
			}

			String strOrderBy = (StringUtils.isBlank(extraOrderBy)) ? " " : " " + (SQLKeyword.ORDER_BY + " " + extraOrderBy);

			if (hasOrderByPlaceholder) {
				parsedQuery = parsedQuery.replaceAll(SQL_PLACEHOLDER_ORDER_BY, strOrderBy);
			} else {
				parsedQuery += extraOrderBy;
			}
			if (dialect == DatabaseDialect.HSQLDB || dialect == DatabaseDialect.POSTGRES) {
				parsedQuery = StringUtils.replaceAll(parsedQuery, "WM_CONCAT", "GROUP_CONCAT");
			}
		} else {
			return parsedQuery;
		}

		if (removeSpecialChars) {
			parsedQuery = removeSpecialCharacters(parsedQuery);
		}

		return parsedQuery;
	}
}
