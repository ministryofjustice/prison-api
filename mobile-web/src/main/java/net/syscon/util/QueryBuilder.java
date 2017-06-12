package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author om.pandey
 *
 */
public class QueryBuilder {
	private Builder queryBuilder;
	
	private QueryBuilder(Builder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}
	
	public String toString() {
		return this.queryBuilder.baseQuery.toString();
	}

	public static class Builder {
	    public static final String SQL_PLACEHOLDER_QUERY = ":query";
	    public static final String SQL_PLACEHOLDER_OFFSET = ":offset";
	    public static final String SQL_PLACEHOLDER_ORDER_BY = ":orderby";
	    public static final String SQL_PLACEHOLDER_RECORD_COUNT = "record_count";

		private StringBuilder baseQuery;
		private Map<String,FieldMapper> fieldMap;
		private boolean preOracle12;
		
		public Builder(String baseQuery, Map<String, FieldMapper> fieldMap, boolean preOracle12) {
			this.baseQuery = new StringBuilder(baseQuery);
			this.fieldMap = fieldMap;
			this.preOracle12 = preOracle12;
		}
		
		public Builder addQuery(String query) {
            StringBuilder builtQuery = new StringBuilder();

			if (StringUtils.isNotBlank(query)) {
                List<String> queryList = QueryUtil.checkPrecdencyAndSplit(query, new ArrayList<>());

                queryList.stream().filter(queryItem -> {
                    return queryItem.length() > 0;
                }).forEach(queryItem -> {
                    if (queryItem.contains("(") && queryItem.contains(")")) {
                        queryItem = queryItem.replace("(", "").replace(")", "");
                        builtQuery.append(QueryUtil.prepareQuery(queryItem, true, fieldMap));
                    } else {
                        builtQuery.append(QueryUtil.prepareQuery(queryItem, false, fieldMap));
                    }
                });
            }

            int queryPlaceholderPos = baseQuery.indexOf(SQL_PLACEHOLDER_QUERY);

            if (queryPlaceholderPos == -1) {
                baseQuery.append(builtQuery);
            } else {
                while (queryPlaceholderPos != -1) {
                    baseQuery.replace(queryPlaceholderPos, queryPlaceholderPos + SQL_PLACEHOLDER_QUERY.length(), builtQuery.toString());
                    queryPlaceholderPos += builtQuery.length();
                    queryPlaceholderPos = baseQuery.indexOf(SQL_PLACEHOLDER_QUERY, queryPlaceholderPos);
                }
            }

			return this;
		}
		
		public Builder addRowCount() {
		    if (baseQuery.length() > 0) {
                String sqlQuery = this.baseQuery.toString().toLowerCase();

                // Only include row count statement if base query does not already include it
                if (sqlQuery.indexOf(SQL_PLACEHOLDER_RECORD_COUNT) == -1) {
                    int firstSelectLocation = sqlQuery.indexOf("select");
                    String rowCountQuery = "  COUNT(*) OVER() RECORD_COUNT, ";
                    baseQuery.insert(firstSelectLocation + 6, rowCountQuery);
                }
            }

			return this;
		}
		
		public  Builder addPagedQuery() {
			if (baseQuery.length() > 0) {
                String sqlQuery = this.baseQuery.toString().toLowerCase();

			    // Only include pagination if base query does not already include it
                if (sqlQuery.indexOf(SQL_PLACEHOLDER_OFFSET) == -1) {
                    if (preOracle12) {
                        baseQuery.insert(0, "select * from ( select QRY_SQL.*, ROWNUM rnum from ( ");
                        baseQuery.append(" ) QRY_SQL where ROWNUM <= :limit) where rnum >= :offset");
                    } else {
                        baseQuery.append(" OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
                    }
                }
			}

			return this;
		}

		public Builder addOrderBy(boolean isAscending, String...fields) {
		    if (baseQuery.length() > 0) {
                String sqlQuery = this.baseQuery.toString().toLowerCase();

                String key = fieldMap.entrySet().stream()
                        .filter(entry -> {
                            return entry.getValue().getName().equals(fields[0]);
                        })
                        .map(Map.Entry::getKey)
                        .findAny()
                        .orElse(null);

                String strOrderBy = (key == null) ? "" : " ORDER BY " + key + " " + (isAscending ? "" : "DESC");

                int orderByPlaceholderPos = sqlQuery.indexOf(SQL_PLACEHOLDER_ORDER_BY);

                if (orderByPlaceholderPos == -1) {
                    baseQuery.append(strOrderBy);
                } else {
                    baseQuery.replace(orderByPlaceholderPos, orderByPlaceholderPos + SQL_PLACEHOLDER_ORDER_BY.length(), strOrderBy);
                }
            }

			return this;
		}
		
		public String build() {
			QueryBuilder queryBuilder = new QueryBuilder(this);
			return queryBuilder.toString();
		}
	}
}
