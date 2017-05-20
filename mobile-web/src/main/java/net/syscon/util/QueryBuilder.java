package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;

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
		private StringBuilder baseQuery;
		private Map<String,FieldMapper> fieldMap;
		private boolean preOracle12;
		
		public Builder(String baseQuery, Map<String, FieldMapper> fieldMap, boolean preOracle12) {
			this.baseQuery = new StringBuilder(baseQuery);
			this.fieldMap = fieldMap;
			this.preOracle12 = preOracle12;
		}
		
		public Builder addQuery(String query) {
			if(null!=query && query.length()>0) {
				List<String> queryList = QueryUtil.checkPrecdencyAndSplit(query, new ArrayList<String>());
				queryList.stream().filter(queryItem ->{return queryItem.length()>0;}).forEach(queryItem -> {
					if(queryItem.contains("(") && queryItem.contains(")")) {
						queryItem = queryItem.replace("(", "").replace(")", "");
						baseQuery.append(QueryUtil.prepareQuery(queryItem, true, fieldMap));
					} else {
						baseQuery.append(QueryUtil.prepareQuery(queryItem, false, fieldMap));
					}
				});
			}
			return this;
		}
		
		public Builder addRowCount() {
			String sqlQuery = this.baseQuery.toString().toLowerCase();
			int firstSelectLocation = sqlQuery.indexOf("select");
			String rowCountQuery = "  COUNT(*) OVER() RECORD_COUNT, ";
			baseQuery.insert(firstSelectLocation + 6, rowCountQuery);
			return this;
		}
		
		public  Builder addPagedQuery() {
			if (baseQuery.length() > 0) {
				if (preOracle12) {
					baseQuery.insert(0, "select * from ( select QRY_SQL.*, ROWNUM rnum from ( ");
					baseQuery.append(" ) QRY_SQL where ROWNUM <= :limit) where rnum >= :offset");
				} else {
					baseQuery.append(" OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
				}
			}
			return this;
		}

		public Builder addOrderBy(boolean isAscending, String...fields) {
			String key = fieldMap.entrySet().stream()
								.filter(entry -> {return entry.getValue().getName().equals(fields[0]);})
								.map(Map.Entry::getKey)
								.findAny()
								.orElse(null);
			String order = isAscending?"":"DESC";
			if(key != null) {
				baseQuery.append(" ORDER BY "+key+" "+order);
			}
			return this;
		}
		
		public String build() {
			QueryBuilder queryBuilder = new QueryBuilder(this);
			return queryBuilder.toString();
		}
	}
}
