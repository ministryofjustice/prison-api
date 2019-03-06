package net.syscon.elite.util;

import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.MarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.converters.UnmarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Allow cucumber to transparently convert strings to/from LocalTime type,
 */
public class LocalTimeConverter implements Converter {
    public boolean canConvert(final Class type) {
        return LocalTime.class.isAssignableFrom(type);
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void marshal(final Object value, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        if (value != null) {
            final var time = (LocalTime) value;
            final var result = time.format(dateTimeFormatter);
            writer.setValue(result);
        }
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final var value = reader.getValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (value.equalsIgnoreCase("now")) {
            return LocalTime.now();
        }
        return LocalTime.parse(value, dateTimeFormatter);
    }
}
