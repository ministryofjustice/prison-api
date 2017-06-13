package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Optional;

public class OracleQueryBuilder extends AbstractQueryBuilder {

	@Value("${schema.pre.oracle12:false}")
	private boolean preOracle12;

	public OracleQueryBuilder(final String initialSQL, final Map<String, FieldMapper> fieldMap) {
		super(initialSQL, fieldMap);
	}

	public String build() {
		StringBuilder result = new StringBuilder();
		Optional<SQLKeyword> statementType = getStatementType();

		if (Optional.of(SQLKeyword.SELECT).equals(statementType)) {
			// Wrap the initial Query ...
			if (includeRowCount || extraWhere.length() > 0 || extraOrderBy.length() > 0) {
				result.append("SELECT QRY_ALIAS.* FROM (\n").append(initialSQL).append("\n) QRY_ALIAS\n");

				if (includeRowCount) {
					result.insert(7, "COUNT(*) OVER() RECORD_COUNT, ");
				}
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
				if (preOracle12) {
					result.insert(0, "SELECT * FROM (SELECT QRY_PAG.*, ROWNUM rnum FROM ( ");
					result.append(" ) QRY_PAG WHERE ROWNUM <= (:offset+:limit)) WHERE rnum >= (:offset+1)");
				} else {
					result.append("\nOFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
				}
			}
		} else {
			return initialSQL;
		}

		return result.toString();
	}
}
