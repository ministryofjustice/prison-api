package net.syscon.util;


import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateTimeConverter {
	public static final String ISO_LOCAL_DATE_FORMAT_PATTERN = "YYYY-MM-DD";

	/**
	 * Convert a {@code java.time.LocalDate} to Date
	 * @param localDate date only
	 * @return Date with date only information
	 */
    public static Date toDate(final LocalDate localDate) {
		if (localDate != null) {
			return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		return null;
	}

	/**
	 * Convert a {@code java.time.LocalDateTime} to Date
	 * @param localDateTime Date time
	 * @return Date with date and time information
	 */
    public static Date toDate(final LocalDateTime localDateTime) {
		Date result = null;
		if (localDateTime != null) {
			result = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		}
		return result;
	}

    public static Timestamp fromLocalDateTime(final LocalDateTime localDateTime) {
	    if (localDateTime == null) {
	        return null;
	    }
		return Timestamp.valueOf(localDateTime);
	}

    public static Timestamp now(final ZoneOffset zoneOffset) {
        final var now = Instant.now().atOffset(zoneOffset).toLocalDateTime();

		return Timestamp.valueOf(now);
	}

    public static java.sql.Date fromTimestamp(final Timestamp timestamp) {
	    if (timestamp == null) {
            return null;
        }
        final var localDateTime = timestamp.toLocalDateTime();

		return java.sql.Date.valueOf(localDateTime.toLocalDate());
	}

    public static LocalDateTime fromISO8601DateTimeToLocalDateTime(final String iso8601DateTime, final ZoneOffset zoneOffset) {
        if (StringUtils.isBlank(iso8601DateTime)) {
            return null;
        }
        LocalDateTime ldt;
		try {
            final var odt = OffsetDateTime.parse(iso8601DateTime);

			ldt = odt.toInstant().atOffset(zoneOffset).toLocalDateTime();
        } catch (final DateTimeParseException dtpex) {
			// Perhaps input lacks offset-from-UTC. Try parsing as a local date-time.
			ldt = LocalDateTime.parse(iso8601DateTime);
		}
		return ldt;
	}

    public static LocalDateTime fromStringToLocalDateTime(final String strLocalDate, final String localDateTimeFormat) {
        if (StringUtils.isBlank(strLocalDate)) {
            return null;
        }
        LocalDateTime ldt;
		try {
            final var formatter = DateTimeFormatter.ofPattern(localDateTimeFormat);
			ldt = LocalDateTime.parse(strLocalDate, formatter);
        } catch (final DateTimeParseException dtpex) {
			ldt = null;
		}
		return ldt;
	}

    /**
     * Converts an ISO8601 formatted date string (YYYY-MM-DD) to a {@link LocalDate}.
     *
     * @param iso8601DateString date string to convert.
     * @return LocalDate or {@code null} if provided date string is blank or {@code null}.
     */
    public static LocalDate fromISO8601DateString(final String iso8601DateString) {
        final LocalDate outDate;

		if (StringUtils.isNotBlank(iso8601DateString)) {
			outDate = LocalDate.parse(iso8601DateString, DateTimeFormatter.ISO_LOCAL_DATE);
		} else {
			outDate = null;
		}

		return outDate;
	}

	/**
	 * Converts date represented by provided date object to a {@link LocalDate}. Any time component in provided date
	 * object is assumed to be at UTC offset.
	 *
	 * @param date an instance of {@code java.sql.Timestamp} or {@code java.util.Date}
	 * @return LocalDate or {@code null} if provided date object is {@code null}.
	 * @throws IllegalArgumentException if provided date object is not of a supported type.
	 */
    public static LocalDate toISO8601LocalDate(final Object date) {
		LocalDate localDate = null;

		if (date != null) {
			if (date instanceof java.sql.Date) {
				localDate = ((java.sql.Date) date).toLocalDate();
			} else if (date instanceof Date) {
				localDate = ((Date) date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			} else {
				throw new IllegalArgumentException("Cannot convert [" + date.getClass().getName() + "] to a LocalDate.");
			}
		}

		return localDate;
	}

	/**
	 * Converts date and time represented by provided dateTime object to a {@link LocalDateTime}.
	 *
	 * @param dateTime an instance of {@code java.sql.Timestamp} or {@code java.util.Date}.
	 * @return a {@link LocalDateTime} or {@code null} if provided dateTime object is {@code null}.
	 * @throws IllegalArgumentException if provided dateTime object is not of a supported type.
	 */
    public static LocalDateTime toISO8601LocalDateTime(final Object dateTime) {
		LocalDateTime localDateTime = null;

		if (dateTime != null) {
			if (dateTime instanceof Timestamp) {
				localDateTime = ((Timestamp) dateTime).toLocalDateTime();
			} else if (dateTime instanceof Date) {
				localDateTime = ((Date) dateTime).toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
			} else {
				throw new IllegalArgumentException("Cannot convert [" + dateTime.getClass().getName() + "] to a LocalDateTime.");
			}
		}

		return localDateTime;
	}

	/**
	 * Get Age from date of birth
	 * @param dateOfBirth
	 * @return age in years
	 */
    public static int getAge(final LocalDate dateOfBirth) {
        final var period = Period.between(dateOfBirth, LocalDate.now());
		return period.getYears();
	}
}
