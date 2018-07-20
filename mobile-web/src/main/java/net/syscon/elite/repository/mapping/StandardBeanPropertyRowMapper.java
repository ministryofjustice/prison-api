package net.syscon.elite.repository.mapping;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.util.DateTimeConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class StandardBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {
    private Map<String,FieldMapper> fieldMap;

    /**
     * Create a new {@code StandardBeanPropertyRowMapper}, accepting unpopulated
     * properties in the target bean.
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public StandardBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    public Map<String,FieldMapper> getFieldMap() {
        return fieldMap;
    }

    /**
     * Initialize the mapping metadata for the given class.
     * @param mappedClass the mapped class
     */
    @Override
    protected void initialize(Class<T> mappedClass) {
        super.initialize(mappedClass);

        ImmutableMap.Builder<String,FieldMapper> fieldMapBuilder = new ImmutableMap.Builder<>();
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);

        for (PropertyDescriptor pd : pds) {
            if ((pd.getWriteMethod() != null) && !StringUtils.equals(pd.getName(), "additionalProperties")) {
                String underscoredName = underscoreName(pd.getName());

                fieldMapBuilder.put(underscoredName.toUpperCase(), new FieldMapper(pd.getName()));
            }
        }

        this.fieldMap = fieldMapBuilder.build();
    }

    @Override
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        return super.mapRow(rs, rowNumber);
    }

    @Override
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        Object colValue;
        Class<?> clazz = pd.getPropertyType();

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
