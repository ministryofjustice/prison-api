package net.syscon.util;

import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.web.api.model.PageMetaData;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static net.syscon.elite.core.Constants.RECORD_COUNT;

public class MetaDataFactory {
	
	public static <T> PageMetaData createMetaData(int limit, int offset, List<T> collection) {
		final PageMetaData metaData = new PageMetaData();
		metaData.setLimit(Long.valueOf(limit));
		metaData.setOffset(Long.valueOf(offset));
		metaData.setTotalRecords(Long.valueOf(0));
		if (!collection.isEmpty()) {
			
			Long recordCount = null;
			T item = collection.get(0);
			boolean accessible = false;
			Field field = null;
			
			try {
				field = ReflectionUtils.findField(item.getClass(), "additionalProperties");
				accessible = field.isAccessible();
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<String, Object> additionalProperties = (Map<String, Object>) ReflectionUtils.getField(field, item);
				if (additionalProperties != null) {
				    Number value = (Number) additionalProperties.get(RECORD_COUNT);
					if (value != null)  {
						recordCount = value.longValue();
					}
				}
			} finally {
				if (field != null) {
					field.setAccessible(accessible);
				}
			}
			
			if (recordCount == null) {
				throw new EliteRuntimeException(String.format("There is no \"additionalProperties\" field on the class %s", item.getClass().getName()));
			} else {
				metaData.setTotalRecords(recordCount);
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
		}

		return metaData;
	}
}
