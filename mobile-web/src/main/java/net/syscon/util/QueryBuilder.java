package net.syscon.util;

import java.util.Map;

import net.syscon.elite.persistence.mapping.FieldMapper;

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
		
		public Builder(String baseQuery, Map<String,FieldMapper> fieldMap) {
			this.baseQuery = new StringBuilder(baseQuery);
			this.fieldMap = fieldMap;
		}
		
		public  Builder addPagedQuery() {
			if (baseQuery.length() > 0) {
				baseQuery.append(" OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
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
