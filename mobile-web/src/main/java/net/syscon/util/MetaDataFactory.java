package net.syscon.util;

import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.v2.api.model.PageMetaData;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static net.syscon.elite.core.Constants.RECORD_COUNT;

public class MetaDataFactory {

	public static <T> PageMetaData createMetaData(int limit, int offset, List<T> collection) {
		final PageMetaData metaData = new PageMetaData();
		metaData.setLimit((long) limit);
		metaData.setOffset((long) offset);
		metaData.setTotalRecords(getTotalRecords(collection));
		return metaData;
	}

    public static <T> Long peekTotalRecords(List<T> collection) {
        return getTotalRecords(collection, false);
	}

	public static <T> Long getTotalRecords(List<T> collection) {
		return getTotalRecords(collection, true);
	}

    public static <T> Long getTotalRecords(List<T> collection, boolean removeRecordCountAttr) {
        Long recordCount;

        if (collection == null || collection.isEmpty()) {
            recordCount = 0L;
        } else {
            final T item = collection.get(0);

            recordCount = findRecordCount(item);

            if (recordCount == null) {
                throw new EliteRuntimeException(
                        String.format("There is no \"additionalProperties\" field on the class %s",
                                item.getClass().getName()));
            } else if (removeRecordCountAttr) {
                removeRecordCountFromAttr(collection);
            }
        }

        return recordCount;
    }

	private static <T> void removeRecordCountFromAttr(List<T> collection) {
		collection.forEach(element -> {
            Field additionalPropertiesField = ReflectionUtils.findField(element.getClass(), "additionalProperties");
            boolean additionalPropertiesAcessible = additionalPropertiesField.isAccessible();
            additionalPropertiesField.setAccessible(true);
            @SuppressWarnings("unchecked")
			Map<String, Object> additionalProperties = (Map<String, Object>) ReflectionUtils.getField(additionalPropertiesField, element);
            additionalProperties.remove(RECORD_COUNT);
            additionalPropertiesField.setAccessible(additionalPropertiesAcessible);
        });
	}

	private static <T> Long findRecordCount(T item) {
		Field field = null;
		boolean accessible = false;
		try {
			field = ReflectionUtils.findField(item.getClass(), "additionalProperties");
            accessible = field.isAccessible();
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
			Map<String, Object> additionalProperties = (Map<String, Object>) ReflectionUtils.getField(field, item);
            if (additionalProperties != null) {
                Number value = (Number) additionalProperties.get(RECORD_COUNT);
                if (value != null)  {
                    return value.longValue();
                }
            }
        } finally {
            if (field != null) {
                field.setAccessible(accessible);
            }
        }
		return null;
	}
}
