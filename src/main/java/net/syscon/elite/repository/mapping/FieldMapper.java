package net.syscon.elite.repository.mapping;


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

	static final String ADDITIONAL_PROPERTIES = "additionalProperties";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String name;
	private Function<Field, Void> setterFunction;
	private Function<Object, Object> decodeFunction;

	// encode value for query
	private Function<String, String> encodeFunction;

    public FieldMapper(final String name) {
		this.name = name;
	}

    public FieldMapper(final String name, final Function<Object, Object> decodeFunction) {
		this.name = name;
		this.decodeFunction = decodeFunction;
	}

    public FieldMapper(final String name, final Function<Object, Object> decodeFunction, final Function<Field, Void> setterFunction) {
		this.name = name;
		this.decodeFunction = decodeFunction;
		this.setterFunction = setterFunction;
	}

    public FieldMapper(final String name, final Function<Object, Object> decodeFunction, final Function<Field, Void> setterFunction,
                       final Function<String, String> encodeFunction) {
		this.name = name;
		this.decodeFunction = decodeFunction;
		this.setterFunction = setterFunction;
		this.encodeFunction = encodeFunction;
	}

    private Object getCompatibleValue(final Field field, final Object value) throws SQLException {
        final var fieldType = field.getType();
		if (decodeFunction != null) {
			return decodeFunction.apply(value);
		} else if (fieldType.isInstance(value)) {
			return value;
		} else if (value instanceof Number) {
			return getNumberValue(value, fieldType);
		} else if (value instanceof java.util.Date) {
			return getDateValue(value, fieldType);
		} else if (value instanceof  Blob && fieldType == byte[].class) {
			return getBlobValue(value);
		}
		return value;
	}

    private Object getBlobValue(final Object value) throws SQLException {
        final var blob = (Blob) value;
        final var length = (int) blob.length();
        final var result = blob.getBytes(1, length);
		blob.free();
		return result;
	}

    private Object getDateValue(final Object value, final Class<?> fieldType) {
		if (fieldType.equals(java.sql.Date.class)) {
			return new java.sql.Date(((java.util.Date) value).getTime());
		} else if (fieldType.equals(java.sql.Time.class)) {
			return new java.sql.Time(((java.util.Date) value).getTime());
		} else if (fieldType.equals(java.sql.Timestamp.class)) {
			return new java.sql.Timestamp(((java.util.Date) value).getTime());
		}
		return null;
	}

	@SuppressWarnings({"squid:S3776", "squid:MethodCyclomaticComplexity"})
    private Object getNumberValue(final Object value, final Class<?> fieldType) {
        final var numberValue = (Number) value;
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
		if (Boolean.TYPE.equals(fieldType) || Boolean.class.equals(fieldType))
			return numberValue.byteValue() != 0;
		if (BigDecimal.class.equals(fieldType))
			return new BigDecimal(numberValue.toString());
		if (BigInteger.class.equals(fieldType))
			return new BigInteger(numberValue.toString());
		if (Float.TYPE.equals(fieldType) || Float.class.equals(fieldType))
			return numberValue.floatValue();
		if (Double.TYPE.equals(fieldType) || Double.class.equals(fieldType))
			return numberValue.doubleValue();
		return null;
	}

    private Field findField(final Class clazz) {
		if (clazz != null) {
			try {
				return clazz.getDeclaredField(name);
            } catch (final NoSuchFieldException e) {
				if (clazz.getSuperclass() != null) {
					return findField(clazz.getSuperclass());
				}
			}
		}
		return null;
	}

    void setValue(final Object target, final Object value) {
		try {
            final var beanField = findField(target.getClass());
			if (beanField != null) {
                final var accessible = beanField.canAccess(target);
				beanField.setAccessible(true);
				if (setterFunction != null) {
					setterFunction.apply(beanField);
				} else {
                    final var compatibleValue = getCompatibleValue(beanField, value);
					ReflectionUtils.setField(beanField, target, compatibleValue);
				}
				beanField.setAccessible(accessible);
			}
        } catch (final SQLException ex) {
			logger.warn("Failure setting the field \"" + name + "\" on " + target.getClass().getName());
		}
	}

    public String getEncodedValue(final String value) {
        final String encodedValue;

		if (encodeFunction == null) {
			encodedValue = value;
		} else {
			encodedValue = encodeFunction.apply(value);
		}

		return encodedValue;
	}

	public String getName() {
		return name;
	}
}
