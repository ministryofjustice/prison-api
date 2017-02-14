package net.syscon.elite.persistence.repository.impl;


import net.syscon.elite.exception.RowMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Row2BeanRowMapper<T> implements RowMapper<T> {


	private static final Map<MappingInfo, Row2BeanRowMapper> cachedMappings = new ConcurrentHashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(Row2BeanRowMapper.class);

	private String sql;
	private Class<? extends T> type;
	private List<String> sqlToCollumns;
	private Map<String, String> columnsMapping;

	public Row2BeanRowMapper(String sql, Class<? extends T> type, Map<String, String> mappings) {
		this.type = type;
		this.sql = sql;
		this.columnsMapping = new HashMap<>();
		for (Map.Entry<String, String> entry: mappings.entrySet()) {
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


	private Object getCompatibleValue(Field field, ResultSet rs, String columnName) throws SQLException {
		Class<?> fieldType = field.getType();
		Object value = rs.getObject(columnName);
		if (value != null) {
			if (fieldType.isInstance(value)) return value;
			else if (value instanceof Number) {
				Number numberValue = (Number) value;
				if (Long.TYPE.equals(fieldType) || Long.class.equals(fieldType))
					return numberValue.longValue();
				if (Integer.TYPE.equals(fieldType) || Integer.class.equals(fieldType))
					return numberValue.intValue();
				if (Short.TYPE.equals(fieldType) || Short.class.equals(fieldType))
					return numberValue.shortValue();
				if (Byte.TYPE.equals(fieldType) || Byte.class.equals(fieldType))
					return numberValue.byteValue();
				if (Character.TYPE.equals(fieldType) || Character.class.equals(fieldType))
					return (char) numberValue.shortValue();
				if (Boolean.TYPE.equals(fieldType) || boolean.class.equals(fieldType))
					return numberValue.byteValue() == 0 ? false : true;
				if (BigDecimal.class.equals(fieldType))
					return new BigDecimal(numberValue.toString());
				if (BigInteger.class.equals(fieldType))
					return new BigInteger(numberValue.toString());
			}
			if (value instanceof java.util.Date) {
				if (fieldType.equals(java.sql.Date.class)) {
					return new java.sql.Date(((java.util.Date) value).getTime());
				} else if (fieldType.equals(java.sql.Time.class)) {
					return new java.sql.Time(((java.util.Date) value).getTime());
				} else if (fieldType.equals(java.sql.Timestamp.class)) {
					return new java.sql.Timestamp(((java.util.Date) value).getTime());
				}
			}
		}
		return value;
	}




	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			T bean = type.newInstance();
			loadColumns(rs);
			for (String columnName: sqlToCollumns) {
				if (columnsMapping.containsKey(columnName)) {
					String fieldName = columnsMapping.get(columnName);
					try {
						Field beanField = type.getDeclaredField(fieldName);
						if (beanField != null) {
							boolean accessible = beanField.isAccessible();
							beanField.setAccessible(true);
							Object beanValue = getCompatibleValue(beanField, rs, columnName);
							if (beanValue != null) {
								ReflectionUtils.setField(beanField, bean, beanValue);
							}
							beanField.setAccessible(accessible);
						}
					} catch (Exception ex) {
						logger.warn("Failure setting the field " + fieldName + "  on " + type.getName());
					}
				}
				else {
					try {
						Field beanField = type.getDeclaredField("additionalProperties");
						boolean accessible = beanField.isAccessible();
						beanField.setAccessible(true);
						Object value = rs.getObject(columnName);
						if (beanField != null && value != null) {
							Object beanProperty = beanField.get(bean);
							if (beanProperty != null && beanProperty instanceof Map) {
								Map<String, Object> additionalProperties = (Map<String, Object>) beanProperty;
								additionalProperties.put(columnName.toLowerCase(), rs.getObject(columnName));
							}
						}
						beanField.setAccessible(accessible);
					} catch (Exception ex) {
						logger.warn("Failure adding the field "  +  columnName + " on \"additionalProperties\" " + type.getName());
					}
				}
			}
			return bean;
		} catch (Exception ex) {
			throw new RowMappingException(ex.getMessage(), ex);
		}



	}


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


	@SuppressWarnings("unchecked")
	public static <M> RowMapper<M> makeMapping(String sql, Class<M> type, Map<String, String> mappings) {
		MappingInfo mappingInfo = new MappingInfo(sql, type);
		if (!cachedMappings.containsKey(mappingInfo)) {
			cachedMappings.put(mappingInfo, new Row2BeanRowMapper(sql, type, mappings));
		}
		RowMapper<M> mapping = (RowMapper<M>) cachedMappings.get(mappingInfo);
		return mapping;
	}




}
