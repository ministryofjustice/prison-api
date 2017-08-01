package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class OracleQueryBuilder extends AbstractQueryBuilder {

	private static final String ROW_NUM_SQL = "SELECT QRY_PAG.*, ROWNUM rnum FROM ( ";
	private static final String OFFSET_LIMIT_SQL = " ) QRY_PAG WHERE ROWNUM <= :offset+:limit) WHERE rnum >= :offset+1";
	private static final String COUNT_SELECT = "WITH TOTAL_COUNT AS ( SELECT COUNT(*) AS RECORD_COUNT %s ) SELECT * FROM TOTAL_COUNT, (";

	public OracleQueryBuilder(final String initialSQL, final Map<String, FieldMapper> fieldMap, DatabaseDialect dialect) {
		super(initialSQL, fieldMap, dialect);
	}

	public String build() {
		StringBuilder result = new StringBuilder();
		Optional<SQLKeyword> statementType = getStatementType();

		if (Optional.of(SQLKeyword.SELECT).equals(statementType)) {
			// Wrap the initial Query ...
			if (includeRowCount || extraWhere.length() > 0 || extraOrderBy.length() > 0) {
				buildDataCountSql(result);
			} else {
				result.append(initialSQL);
			}

			// Apply the additional conditions defined by the "addQuery" method ...
			if (extraWhere.length() > 0) {
				result.append("WHERE ").append(extraWhere);
			}

			String strOrderBy = (StringUtils.isBlank(extraOrderBy)) ? "" : (SQLKeyword.ORDER_BY + " " + extraOrderBy);

			result.append(strOrderBy);

			// Wrap the query with pagination parameters ...
			if (includePagination) {
				buildPaginationSql(result);
			}

			if (includeRowCount) {
				buildAnsiDataCountSql(result);
			}
			if (removeSpecialChars) {
				result = new StringBuilder(removeSpecialCharacters(result.toString()));
			}

			if (dialect == DatabaseDialect.HSQLDB) {
				result = new StringBuilder(StringUtils.replaceAll(result.toString(), "WM_CONCAT", "GROUP_CONCAT"));
			}
		} else {
			return initialSQL;
		}

		return result.toString();
	}

	private void buildPaginationSql(StringBuilder result) {
		if (dialect == DatabaseDialect.ORACLE_12) {
			result.append("\nOFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
		} else {
			if (dialect != DatabaseDialect.HSQLDB) {
				result.insert(0, "SELECT * FROM ("+ ROW_NUM_SQL);
				result.append(OFFSET_LIMIT_SQL);
			}
		}
	}

	private void buildDataCountSql(StringBuilder result) {
		result.append("SELECT QRY_ALIAS.* FROM (\n").append(initialSQL).append("\n) QRY_ALIAS\n");

		if (includeRowCount) {
            if (dialect != DatabaseDialect.HSQLDB) {
                result.insert(7, "COUNT(*) OVER() RECORD_COUNT, ");
            }
        }
	}

	private void buildAnsiDataCountSql(StringBuilder result) {
		if (dialect == DatabaseDialect.HSQLDB) {
            final String criteria = QueryUtil.getCriteriaFromQuery(initialSQL);
            final StringBuilder rowCountStr = new StringBuilder(String.format(COUNT_SELECT, criteria));

            if (includePagination) {
                rowCountStr.append(ROW_NUM_SQL);
                result.append(OFFSET_LIMIT_SQL);
            }
            result.insert(0, rowCountStr.toString());
        }
	}
}
