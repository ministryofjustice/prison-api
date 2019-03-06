package net.syscon.elite.web.provider;

import org.junit.Test;

import javax.ws.rs.ext.ParamConverter;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocalDateTimeProviderTest {
    private final LocalDateTimeProvider provider = new LocalDateTimeProvider();
    private final ParamConverter<LocalDateTime> converter = provider.getConverter(LocalDateTime.class, null, null);

    @Test
    public void testStringToLocalDateTime() {
        final var localDateTime = converter.fromString("2018-02-27T01:02:03");
        assertEquals("2018-02-27T01:02:03", localDateTime.toString());
    }

    @Test
    public void testStringToLocalDateTimeNull() {
        assertNull(converter.fromString(null));
    }

    @Test(expected = DateTimeParseException.class)
    public void testStringToLocalDateTimeInvalid() {
        converter.fromString("xxxx");
    }

    @Test(expected = DateTimeParseException.class)
    public void testStringToLocalDateTimeInvalidMissingMinutes() {
        converter.fromString("2018-02-27T01");
    }

    @Test(expected = DateTimeParseException.class)
    public void testStringToLocalDateTimeInvalidMissingDate() {
        converter.fromString("T01:02:03");
    }

    @Test(expected = DateTimeParseException.class)
    public void testStringToLocalDateTimeInvalidMissingTee() {
        converter.fromString("2018-02-27 01:02:03");
    }

    @Test
    public void testStringToLocalDateTimeMissingSeconds() {
        final var localDateTime = converter.fromString("2018-02-27T01:02");
        assertEquals("2018-02-27T01:02", localDateTime.toString());
    }

    @Test
    public void testLocalDateTimeToStringSeconds() {
        final var value = converter.toString(LocalDateTime.of(2018, Month.APRIL, 18, 13, 14, 15));
        assertEquals("2018-04-18T13:14:15", value);
    }

    @Test
    public void testLocalDateTimeToStringNoSeconds() {
        final var value = converter.toString(LocalDateTime.of(2018, Month.APRIL, 18, 13, 14));
        assertEquals("2018-04-18T13:14:00", value);
    }

    @Test
    public void testLocalDateTimeToStringNull() {
        assertNull(converter.toString(null));
    }
}
