package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryBuilder {
	
	private final Builder queryBuilder;

	private QueryBuilder(final Builder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	@Override
	public String toString() {
		return queryBuilder.build();
	}

	public static class Builder {
		
		private final String initialSQL;;
		private final StringBuilder extraWhere = new StringBuilder();
		private final StringBuilder extraOrderBy = new StringBuilder();
		
		private boolean includePagination = false;
		private boolean includeRowCount = false;

		private final Map<String, FieldMapper> fieldMap;
		private final boolean preOracle12;

		public Builder(final String initialSQL, final Map<String, FieldMapper> fieldMap, final boolean preOracle12) {
			this.initialSQL = initialSQL;
			this.fieldMap = fieldMap;
			this.preOracle12 = preOracle12;
		}

		public Builder addQuery(final String query) {
			if (null != query && query.length() > 0) {
				final List<String> queryList = QueryUtil.checkPrecdencyAndSplit(query, new ArrayList<String>());
				queryList.stream().filter(queryItem -> {
					return queryItem.length() > 0;
				}).forEach(queryItem -> {
					if (queryItem.contains("(") && queryItem.contains(")")) {
						queryItem = queryItem.replace("(", "").replace(")", "");
						extraWhere.append(QueryUtil.prepareQuery(queryItem, true, fieldMap));
					} else {
						extraWhere.append(QueryUtil.prepareQuery(queryItem, false, fieldMap));
					}
				});
			}
			return this;
		}
		
		public Builder addOrderBy(final boolean isAscending, final String... fields) {
			final String key = fieldMap.entrySet().stream().filter(entry -> {
				return entry.getValue().getName().equals(fields[0]);
			}).map(Map.Entry::getKey).findAny().orElse(null);
			final String order = isAscending ? "" : "DESC";
			if (key != null) {
				extraOrderBy.append(key + " " + order);
			}
			return this;
		}

		public Builder addRowCount() {
			this.includeRowCount = true;
			return this;
		}

		public Builder addPagedQuery() {
			this.includePagination = true;
			return this;
		}


		private String getStatementType() {
			String modifiedSQL = initialSQL.toUpperCase().replace('\t', ' ').replace('\r', ' ');
			String[] lines = modifiedSQL.split("\\n");
			for (int i = 0; i < lines.length; i++) {
				String s = lines[i].trim().toUpperCase();
				if (s.startsWith("SELECT")) return "SELECT";
				if (s.startsWith("DELETE")) return "DELETE";
				if (s.startsWith("INSERT")) return "INSERT";
				if (s.startsWith("UPDATE")) return "UPDATE";
			}
			return null;
		}

		

		public String build() {

			final StringBuilder result = new StringBuilder();

			if ("SELECT".equals(getStatementType())) {

				// Wrap the initial Query ...
				if (this.includeRowCount) {
					result.append("SELECT QRY_ALIAS.*, COUNT(*) OVER() RECORD_COUNT FROM (\n").append(initialSQL).append("\n) QRY_ALIAS\n");
				} else {
					result.append("SELECT QRY_ALIAS.* FROM (\n").append(initialSQL).append("\n) QRY_ALIAS\n");
				}

				// Apply the additional conditions defined by the "addQuery" method ...
				if (extraWhere.length() > 0) {
					result.append("WHERE ").append(extraWhere);
				}

				if (extraOrderBy.length() > 0) {
					result.append("ORDER BY ").append(extraOrderBy);
				}

				// Wrap the query with pagination parameters ...
				if (this.includePagination) {
					if (preOracle12) {
						result.insert(0, "SELECT * FROM (SELECT QRY_PAG.*, ROWNUM rnum FROM ( ");
						result.append(" ) QRY_PAG WHERE ROWNUM <= :limit) WHERE rnum >= :offset");
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
}
