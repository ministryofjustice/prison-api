package uk.gov.justice.hmpps.prison.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

public class Extractors {
    public static Function<Object, String> extractString(final String field) {
        return m -> ((Map<String, String>) m).get(field);
    }

    public static Function<Object, Integer> extractInteger(final String field) {
        return m -> ((Map<String, BigDecimal>) m).get(field).intValue();
    }

    public static Function<Object, Long> extractLong(final String field) {
        return m -> ((Map<String, BigDecimal>) m).get(field).longValue();
    }

    public static Function<Object, LocalDate> extractDate(final String field) {
        return m -> ((Map<String, Timestamp>) m).get(field).toLocalDateTime().toLocalDate();
    }
}
