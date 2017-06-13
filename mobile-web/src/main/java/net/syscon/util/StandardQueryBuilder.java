package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class StandardQueryBuilder extends AbstractQueryBuilder {

	public StandardQueryBuilder(final String initialSQL, final Map<String, FieldMapper> fieldMap) {
		super(initialSQL, fieldMap);

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
			if (hasWhereQueryPlaceholder) {
				parsedQuery = parsedQuery
						.replaceAll(SQL_PLACEHOLDER_WHERE_QUERY, SQLKeyword.WHERE + " " + extraWhere);
			}

			if (hasAndQueryPlaceholder) {
				parsedQuery = parsedQuery
						.replaceAll(SQL_PLACEHOLDER_AND_QUERY, SQLKeyword.AND + " " + extraWhere);
			}

			if (hasOrQueryPlaceholder) {
				parsedQuery = parsedQuery
						.replaceAll(SQL_PLACEHOLDER_OR_QUERY, SQLKeyword.OR + " " + extraWhere);
			}

			String strOrderBy = (StringUtils.isBlank(extraOrderBy)) ? "" : (SQLKeyword.ORDER_BY + " " + extraOrderBy);

			if (hasOrderByPlaceholder) {
				parsedQuery = parsedQuery.replaceAll(SQL_PLACEHOLDER_ORDER_BY, strOrderBy);
			} else {
				parsedQuery += extraOrderBy;
			}
		} else {
			return parsedQuery;
		}

		return parsedQuery;
	}
}
