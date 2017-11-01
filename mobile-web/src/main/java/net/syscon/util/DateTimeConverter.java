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
	public static Date toDate(LocalDate localDate) {
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
	public static Date toDate(LocalDateTime localDateTime) {
		Date result = null;
		if (localDateTime != null) {
			result = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		}
		return result;
	}

	public static Timestamp fromLocalDateTime(LocalDateTime localDateTime) {
		return Timestamp.valueOf(localDateTime);
	}

	public static Timestamp now(ZoneOffset zoneOffset) {
		LocalDateTime now = Instant.now().atOffset(zoneOffset).toLocalDateTime();

		return Timestamp.valueOf(now);
	}

	public static java.sql.Date fromTimestamp(Timestamp timestamp) {
		LocalDateTime localDateTime = timestamp.toLocalDateTime();

		return java.sql.Date.valueOf(localDateTime.toLocalDate());
	}

	public static Timestamp fromISO8601DateTime(String iso8601DateTime, ZoneOffset zoneOffset) {
		LocalDateTime ldt;

		ldt = fromISO8601DateTimeToLocalDateTime(iso8601DateTime, zoneOffset);
		return Timestamp.valueOf(ldt);
	}

	public static LocalDateTime fromISO8601DateTimeToLocalDateTime(String iso8601DateTime, ZoneOffset zoneOffset) {
		LocalDateTime ldt;
		try {
			OffsetDateTime odt = OffsetDateTime.parse(iso8601DateTime);

			ldt = odt.toInstant().atOffset(zoneOffset).toLocalDateTime();
		} catch (DateTimeParseException dtpex) {
			// Perhaps input lacks offset-from-UTC. Try parsing as a local date-time.
			ldt = LocalDateTime.parse(iso8601DateTime);
		}
		return ldt;
	}

	public static LocalDateTime fromStringToLocalDateTime(String strLocalDate, String localDateTimeFormat) {
		LocalDateTime ldt;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(localDateTimeFormat);
			ldt = LocalDateTime.parse(strLocalDate, formatter);
		} catch (DateTimeParseException dtpex) {
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
	public static LocalDate fromISO8601DateString(String iso8601DateString) {
		LocalDate outDate;

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
	public static LocalDate toISO8601LocalDate(Object date) {
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
	public static LocalDateTime toISO8601LocalDateTime(Object dateTime) {
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
	 * Converts date and time represented by provided dateTime object to an {@link OffsetDateTime}. Treats
	 * provided dateTime as a local UTC dateTime.
	 *
	 * @param dateTime an instance of {@code java.sql.Timestamp} or {@code java.util.Date}.
	 * @return an {@link OffsetDateTime} or {@code null} if provided dateTime object is {@code null}.
	 * @throws IllegalArgumentException if provided dateTime object is not of a supported type.
	 */
	public static OffsetDateTime toISO8601OffsetDateTime(Object dateTime) {
		LocalDateTime ldt = toISO8601LocalDateTime(dateTime);

		return OffsetDateTime.of(ldt, ZoneOffset.UTC);
	}

	/**
	 * Get Age from date of birth
	 * @param dateOfBirth
	 * @return age in years
	 */
	public static int getAge(LocalDate dateOfBirth) {
		Period period = Period.between(dateOfBirth, LocalDate.now());
		return period.getYears();
	}
}
