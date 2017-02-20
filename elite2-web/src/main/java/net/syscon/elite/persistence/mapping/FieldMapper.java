package net.syscon.elite.persistence.mapping;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.function.Function;

public class FieldMapper {

	public static final String ADDITIONAL_PROPERTIES = "additionalProperties";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String name;
	private Function<Field, Void> setterFunction;
	private Function<Object, Object> decodeFunction;


	public FieldMapper(String name) {
		this.name = name;
	}

	public FieldMapper(String name,  Function<Object, Object> decodeFunction) {
		this.name = name;
		this.decodeFunction = decodeFunction;
	}

	public FieldMapper(String name,  Function<Object, Object> decodeFunction, Function<Field, Void> setterFunction) {
		this.name = name;
		this.decodeFunction = decodeFunction;
		this.setterFunction = setterFunction;
	}

	public void setSetterFunction(Function<Field, Void> setterFunction) {
		this.setterFunction = setterFunction;
	}

	public void setDecodeFunction(Function<Object, Object> decodeFunction) {
		this.decodeFunction = decodeFunction;
	}

	private Object getCompatibleValue(Field field, Object value) throws SQLException {
		Class<?> fieldType = field.getType();
		if (decodeFunction != null) {
			return decodeFunction.apply(value);
		}
		else if (fieldType.isInstance(value)) {
			return value;
		} else if (value instanceof Number) {
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
		else if (value instanceof java.util.Date) {
			if (fieldType.equals(java.sql.Date.class)) {
				return new java.sql.Date(((java.util.Date) value).getTime());
			} else if (fieldType.equals(java.sql.Time.class)) {
				return new java.sql.Time(((java.util.Date) value).getTime());
			} else if (fieldType.equals(java.sql.Timestamp.class)) {
				return new java.sql.Timestamp(((java.util.Date) value).getTime());
			}
		} else if (value instanceof  Blob && fieldType == byte[].class) {
			Blob blob = (Blob) value;
			int length = (int) blob.length();
			value = blob.getBytes(1, length);
			blob.free();
		}
		return value;
	}

	public void setValue(Object target, Object value) {
		if (value != null && target != null) {
			try {
				Field beanField = target.getClass().getDeclaredField(name);
				if (beanField != null) {
					final boolean accessible = beanField.isAccessible();
					beanField.setAccessible(true);
					if (setterFunction != null) {
						setterFunction.apply(beanField);
					} else {
						final Object compatibleValue = getCompatibleValue(beanField, value);
						ReflectionUtils.setField(beanField, target, compatibleValue);
					}
					beanField.setAccessible(accessible);
				}
			} catch (Exception ex) {
				logger.warn("Failure setting the field \"" + name + "\" on " + target.getClass().getName());
			}
		}
	}


}
