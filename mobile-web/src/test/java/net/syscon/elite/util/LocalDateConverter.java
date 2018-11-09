package net.syscon.elite.util;

import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.MarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.converters.UnmarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Allow cucumber to transparently convert strings to/from LocalDate type,
 * e.g. see Award and Alert tests
 */
public class LocalDateConverter implements Converter {
    public boolean canConvert(Class type) {
        return LocalDate.class.isAssignableFrom(type);
    }

    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);

    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        LocalDate date = (LocalDate) value;
        String result = date.format(DEFAULT_DATE_FORMATTER);
        writer.setValue(result);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final String value = reader.getValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (value.equalsIgnoreCase("today") || value.equalsIgnoreCase("now")) {
            return LocalDate.now();
        }
        LocalDate result = LocalDate.parse(value, DEFAULT_DATE_FORMATTER);
        return result;
    }
}
