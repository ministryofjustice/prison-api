package uk.gov.justice.hmpps.prison.repository.mapping;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.jdbc.core.RowMapper;
import uk.gov.justice.hmpps.prison.core.Constants;
import uk.gov.justice.hmpps.prison.exception.RowMappingException;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class Row2BeanRowMapper<T> implements RowMapper<T> {
    private final Class<? extends T> type;
    private final Map<String, FieldMapper> columnsMapping;
    private List<String> sqlToColumns;

    private Row2BeanRowMapper(final Class<? extends T> type, final Map<String, FieldMapper> mappings) {
        this.type = type;
        this.columnsMapping = new HashMap<>();
        for (final var entry : mappings.entrySet()) {
            final var upperKey = entry.getKey() != null ? entry.getKey().toUpperCase() : null;
            this.columnsMapping.put(upperKey, entry.getValue());
        }
    }

    private void loadColumns(final ResultSet rs) {
        if (sqlToColumns == null) {
            final List<String> loadingCols = new ArrayList<>();
            try {
                final var rsmd = rs.getMetaData();
                final var count = rsmd.getColumnCount();
                for (var i = 1; i <= count; i++) {
                    loadingCols.add(rsmd.getColumnName(i).toUpperCase());
                }
                sqlToColumns = loadingCols;
            } catch (final SQLException ex) {
                throw new RowMappingException(ex.getMessage(), ex);
            }
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <M> RowMapper<M> makeMapping(final Class<M> type, final Map<String, FieldMapper> mappings) {
        return new Row2BeanRowMapper(type, mappings);
    }

    private String camelize(final String columnName) {
        var result = WordUtils.capitalizeFully(columnName.toLowerCase(), '_');
        result = (result.substring(0, 1).toLowerCase() + result.substring(1)).replaceAll("_", "");
        return result;
    }


    public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        try {
            final var bean = type.getDeclaredConstructor().newInstance();
            loadColumns(rs);

            for (final var columnName : sqlToColumns) {
                if (!StringUtils.equals(Constants.RECORD_COUNT_COLUMN, columnName)) {
                    final var value = rs.getObject(columnName);

                    if (value != null) {
                        final var fieldMapper = getFieldMapper(bean, columnName, value);

                        fieldMapper.setValue(bean, value);
                    }
                }
            }

            return bean;
        } catch (final Exception ex) {
            throw new RowMappingException(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings({"squid:S00108", "squid:S1166"})
    private FieldMapper getFieldMapper(final T bean, final String columnName, final Object value) {
        var fieldMapper = columnsMapping.get(columnName);
        if (fieldMapper == null) {
            Field candidateField = null;
            final var fieldName = camelize(columnName);
            try {
                candidateField = bean.getClass().getDeclaredField(fieldName);
            } catch (final Exception ex) {
            }
            if (candidateField != null) {
                fieldMapper = new FieldMapper(fieldName);
                columnsMapping.put(columnName, fieldMapper);
            } else {
                fieldMapper = getAdditionalPropertiesFieldMapper(bean, value, fieldName);
            }
        }
        return fieldMapper;
    }

    private FieldMapper getAdditionalPropertiesFieldMapper(final T bean, final Object value, final String fieldName) {
        return new FieldMapper(FieldMapper.ADDITIONAL_PROPERTIES, null, field -> {
            try {
                if (field != null && field.getType().equals(Map.class)) {
                    @SuppressWarnings("unchecked")
                    var additionalProperties = (Map<String, Object>) field.get(bean);
                    if (additionalProperties == null) {
                        additionalProperties = new HashMap<>();
                        field.set(bean, additionalProperties);
                    }
                    additionalProperties.put(fieldName, value);
                }
            } catch (final IllegalArgumentException | IllegalAccessException ex) {
                log.warn("Failure adding the field {} on \"{}\" {}", fieldName, FieldMapper.ADDITIONAL_PROPERTIES, type.getName());
            }
            return null;
        });
    }


}
