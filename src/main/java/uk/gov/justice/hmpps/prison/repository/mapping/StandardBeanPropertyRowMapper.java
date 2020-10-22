package uk.gov.justice.hmpps.prison.repository.mapping;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class StandardBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {
    private Map<String, FieldMapper> fieldMap;

    /**
     * Create a new {@code StandardBeanPropertyRowMapper}, accepting unpopulated
     * properties in the target bean.
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public StandardBeanPropertyRowMapper(final Class<T> mappedClass) {
        super(mappedClass);
    }

    public Map<String, FieldMapper> getFieldMap() {
        return fieldMap;
    }

    /**
     * Initialize the mapping metadata for the given class.
     *
     * @param mappedClass the mapped class
     */

    protected void initialize(final Class<T> mappedClass) {
        super.initialize(mappedClass);

        final var fieldMapBuilder = new ImmutableMap.Builder<String, FieldMapper>();
        final var pds = BeanUtils.getPropertyDescriptors(mappedClass);

        for (final var pd : pds) {
            if ((pd.getWriteMethod() != null) && !StringUtils.equals(pd.getName(), "additionalProperties")) {
                final var underscoredName = underscoreName(pd.getName());

                fieldMapBuilder.put(underscoredName.toUpperCase(), new FieldMapper(pd.getName()));
            }
        }

        this.fieldMap = fieldMapBuilder.build();
    }


    public T mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
        return super.mapRow(rs, rowNumber);
    }


    protected Object getColumnValue(final ResultSet rs, final int index, final PropertyDescriptor pd) throws SQLException {
        final Object colValue;
        final var clazz = pd.getPropertyType();

        if (LocalDate.class == clazz) {
            colValue = DateTimeConverter.toISO8601LocalDate(rs.getObject(index));
        } else if (LocalDateTime.class == clazz) {
            colValue = DateTimeConverter.toISO8601LocalDateTime(rs.getObject(index));
        } else if (Boolean.class == clazz) {
            colValue = "Y".equals(rs.getObject(index));
        } else {
            colValue = super.getColumnValue(rs, index, pd);
        }

        return colValue;
    }
}
