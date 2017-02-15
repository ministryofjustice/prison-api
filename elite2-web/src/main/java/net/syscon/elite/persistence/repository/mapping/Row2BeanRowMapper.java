package net.syscon.elite.persistence.repository.mapping;


import net.syscon.elite.exception.RowMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Row2BeanRowMapper<T> implements RowMapper<T> {

	private static class MappingInfo {
		public String sql;
		public Class<?> type;
		public MappingInfo(String sql, Class<?> type) {
			this.sql = sql;
			this.type = type;
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			MappingInfo that = (MappingInfo) o;
			return Objects.equals(sql, that.sql) &&
					Objects.equals(type, that.type);
		}
		@Override
		public int hashCode() {
			return Objects.hash(sql, type);
		}
	}


	private static final Map<MappingInfo , Row2BeanRowMapper> cachedMappings = new ConcurrentHashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(Row2BeanRowMapper.class);


	private final String sql;
	private final Class<? extends T> type;
	private final Map<String, FieldMapper> columnsMapping;

	private List<String> sqlToCollumns;

	public Row2BeanRowMapper(String sql, Class<? extends T> type, Map<String, FieldMapper> mappings) {
		this.type = type;
		this.sql = sql;
		this.columnsMapping = new HashMap<>();
		for (Map.Entry<String, FieldMapper> entry: mappings.entrySet()) {
			final String upperKey = entry.getKey() != null? entry.getKey().toUpperCase(): null;
			this.columnsMapping.put(upperKey, entry.getValue());
		}
	}

	private void loadColumns(ResultSet rs) {
		if (sqlToCollumns == null) {
			List<String> loadingCols = new ArrayList<>();
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();
				for (int i = 1; i <= count; i++) {
					loadingCols.add(rsmd.getColumnName(i).toUpperCase());
				}
				sqlToCollumns = loadingCols;
			} catch (SQLException ex) {
				throw new RowMappingException(ex.getMessage(), ex);
			}
		}
	}


	@SuppressWarnings("unchecked")
	public static <M> RowMapper<M> makeMapping(String sql, Class<M> type, Map<String, FieldMapper> mappings) {
		MappingInfo mappingInfo = new MappingInfo(sql, type);
		if (!cachedMappings.containsKey(mappingInfo)) {
			cachedMappings.put(mappingInfo, new Row2BeanRowMapper(sql, type, mappings));
		}
		RowMapper<M> mapping = (RowMapper<M>) cachedMappings.get(mappingInfo);
		return mapping;
	}

	@Override
	public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		try {
			T bean = type.newInstance();
			loadColumns(rs);
			for (String columnName: sqlToCollumns) {
				Object value = rs.getObject(columnName);
				if (value != null) {
					FieldMapper fieldMapper = columnsMapping.get(columnName);
					if (fieldMapper == null) {
						fieldMapper = new FieldMapper(FieldMapper.ADDITIONAL_PROPERTIES, null, (field) -> {
							try {
								if (field != null && field.getType().equals(Map.class)) {
									Map<String, Object> additionalProperties = (Map<String, Object>) field.get(bean);
									if (additionalProperties == null) {
										additionalProperties = new HashMap<String, Object>();
										field.set(bean, additionalProperties);
									}
									additionalProperties.put(columnName.toLowerCase(), value);
								}
							} catch (Throwable ex) {
								logger.warn("Failure adding the field "  +  columnName + " on \"" + FieldMapper.ADDITIONAL_PROPERTIES +"\" " + type.getName());
							}
							return null;
						});
					}
					fieldMapper.setValue(bean, value);
				}
			}
			return bean;
		} catch (Exception ex) {
			throw new RowMappingException(ex.getMessage(), ex);
		}
	}



}
