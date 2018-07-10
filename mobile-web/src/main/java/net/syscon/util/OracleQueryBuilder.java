package net.syscon.util;

import net.syscon.elite.repository.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class OracleQueryBuilder extends AbstractQueryBuilder {

	private static final String ROW_NUM_SQL = "SELECT QRY_PAG.*, ROWNUM rnum FROM ( ";
	private static final String OFFSET_LIMIT_SQL = " ) QRY_PAG WHERE ROWNUM <= :offset+:limit) WHERE rnum >= :offset+1";

	public OracleQueryBuilder(final String initialSQL, final Map<String, FieldMapper> fieldMap, DatabaseDialect dialect) {
		super(initialSQL, fieldMap, dialect);
	}

	public String build() {
		StringBuilder result = new StringBuilder();
		Optional<SQLKeyword> statementType = getStatementType();

		if (Optional.of(SQLKeyword.SELECT).equals(statementType)) {
			// Wrap the initial Query ...
			if (includeDirectRowCount) {
				buildDirectRowCountSql(result);
			} else if (includeRowCount || extraWhere.length() > 0 || extraOrderBy.length() > 0) {
				buildDataCountSql(result);
			} else {
				result.append(initialSQL);
			}

			// Apply the additional conditions defined by the "addQuery" method ...
			if (extraWhere.length() > 0) {
				result.append(" WHERE ").append(extraWhere);
			}

			String strOrderBy = (StringUtils.isBlank(extraOrderBy)) ? " " : " " + (SQLKeyword.ORDER_BY + " " + extraOrderBy);

			result.append(strOrderBy);

			// Wrap the query with pagination parameters ...
			if (includePagination) {
				buildPaginationSql(result);
			}

			if (removeSpecialChars) {
				result = new StringBuilder(removeSpecialCharacters(result.toString()));
			}
        } else {
			return initialSQL;
		}

		return result.toString();
	}

	private void buildPaginationSql(StringBuilder result) {
		if (dialect != DatabaseDialect.ORACLE_11) {
			if (dialect == DatabaseDialect.POSTGRES) {
				result.append("\nOFFSET (:offset) ROWS FETCH NEXT (:limit) ROWS ONLY");
			} else {
				result.append("\nOFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
			}
		} else {
			result.insert(0, "SELECT * FROM ("+ ROW_NUM_SQL);
			result.append(OFFSET_LIMIT_SQL);
		}
	}

	private void buildDataCountSql(StringBuilder result) {
		result.append("SELECT QRY_ALIAS.* FROM (\n").append(initialSQL).append("\n) QRY_ALIAS\n");

		if (includeRowCount) {
            result.insert(7, "COUNT(*) OVER() RECORD_COUNT, ");
        }
	}

	private void buildDirectRowCountSql(StringBuilder result) {
		if (includeDirectRowCount) {
			result.append(
					StringUtils.replaceFirst(
							initialSQL, "SELECT", "SELECT COUNT(*) OVER() RECORD_COUNT,"));
		} else {
			result.append(initialSQL);
		}
	}
}
