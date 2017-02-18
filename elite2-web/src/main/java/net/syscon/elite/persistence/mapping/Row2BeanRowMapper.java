package net.syscon.elite.persistence.mapping;


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
		public MappingInfo(final String sql, final Class<?> type) {
			this.sql = sql;
			this.type = type;
		}
		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final MappingInfo that = (MappingInfo) o;
			return Objects.equals(sql, that.sql) &&
					Objects.equals(type, that.type);
		}
		@Override
		public int hashCode() {
			return Objects.hash(sql, type);
		}
	}


	@SuppressWarnings("rawtypes")
	private static final Map<MappingInfo , Row2BeanRowMapper> cachedMappings = new ConcurrentHashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(Row2BeanRowMapper.class);


	private final Class<? extends T> type;
	private final Map<String, FieldMapper> columnsMapping;

	private List<String> sqlToCollumns;

	public Row2BeanRowMapper(final String sql, final Class<? extends T> type, final Map<String, FieldMapper> mappings) {
		this.type = type;
		this.columnsMapping = new HashMap<>();
		for (final Map.Entry<String, FieldMapper> entry: mappings.entrySet()) {
			final String upperKey = entry.getKey() != null? entry.getKey().toUpperCase(): null;
			this.columnsMapping.put(upperKey, entry.getValue());
		}
	}

	private void loadColumns(final ResultSet rs) {
		if (sqlToCollumns == null) {
			final List<String> loadingCols = new ArrayList<>();
			try {
				final ResultSetMetaData rsmd = rs.getMetaData();
				final int count = rsmd.getColumnCount();
				for (int i = 1; i <= count; i++) {
					loadingCols.add(rsmd.getColumnName(i).toUpperCase());
				}
				sqlToCollumns = loadingCols;
			} catch (final SQLException ex) {
				throw new RowMappingException(ex.getMessage(), ex);
			}
		}
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <M> RowMapper<M> makeMapping(final String sql, final Class<M> type, final Map<String, FieldMapper> mappings) {
		final MappingInfo mappingInfo = new MappingInfo(sql, type);
		if (!cachedMappings.containsKey(mappingInfo)) {
			cachedMappings.put(mappingInfo, new Row2BeanRowMapper(sql, type, mappings));
		}
		final RowMapper<M> mapping = cachedMappings.get(mappingInfo);
		return mapping;
	}

	@Override
	public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		try {
			final T bean = type.newInstance();
			loadColumns(rs);
			for (final String columnName: sqlToCollumns) {
				final Object value = rs.getObject(columnName);
				if (value != null) {
					FieldMapper fieldMapper = columnsMapping.get(columnName);
					if (fieldMapper == null) {
						fieldMapper = new FieldMapper(FieldMapper.ADDITIONAL_PROPERTIES, null, (field) -> {
							try {
								if (field != null && field.getType().equals(Map.class)) {
									@SuppressWarnings("unchecked")
									Map<String, Object> additionalProperties = (Map<String, Object>) field.get(bean);
									if (additionalProperties == null) {
										additionalProperties = new HashMap<String, Object>();
										field.set(bean, additionalProperties);
									}
									additionalProperties.put(columnName.toLowerCase(), value);
								}
							} catch (final Throwable ex) {
								logger.warn("Failure adding the field "  +  columnName + " on \"" + FieldMapper.ADDITIONAL_PROPERTIES +"\" " + type.getName());
							}
							return null;
						});
					}
					fieldMapper.setValue(bean, value);
				}
			}
			return bean;
		} catch (final Exception ex) {
			throw new RowMappingException(ex.getMessage(), ex);
		}
	}



}
