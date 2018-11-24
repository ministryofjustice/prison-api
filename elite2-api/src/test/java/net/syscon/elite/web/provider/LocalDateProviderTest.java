package net.syscon.elite.web.provider;

import org.junit.Test;

import javax.ws.rs.ext.ParamConverter;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocalDateProviderTest {
    private final LocalDateProvider provider = new LocalDateProvider();
    private final ParamConverter<LocalDate> converter = provider.getConverter(LocalDate.class, null, null);

    @Test
    public void testStringToLocalDateTime() {
        final LocalDate localDate = converter.fromString("2018-02-27");
        assertEquals("2018-02-27", localDate.toString());
    }

    @Test
    public void testStringToLocalDateNull() {
        assertNull(converter.fromString(null));
    }

    @Test(expected = DateTimeParseException.class)
    public void testStringToLocalDateInvalid() {
        converter.fromString("xxxx");
    }

    @Test
    public void testLocalDateToString() {
        final String date = converter.toString(LocalDate.of(2018,Month.JUNE,15));
        assertEquals("2018-06-15", date);
    }

    @Test
    public void testLocalDateToStringNull() {
        assertNull(converter.toString(null));
    }
}
