package uk.gov.justice.hmpps.prison.util;

import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.MarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.converters.UnmarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Allow cucumber to transparently convert strings to/from LocalDateTime type,
 * e.g. see Award and Alert tests
 */
public class LocalDateTimeConverter implements Converter {

    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm";
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);

    public boolean canConvert(final Class type) {
        return LocalDateTime.class.isAssignableFrom(type);
    }

    public void marshal(final Object value, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        if (value != null) {
            final var date = (LocalDateTime) value;
            final var result = date.format(DEFAULT_DATE_FORMATTER);
            writer.setValue(result);
        }
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final var value = reader.getValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return LocalDateTime.parse(value, DEFAULT_DATE_FORMATTER);
    }
}
