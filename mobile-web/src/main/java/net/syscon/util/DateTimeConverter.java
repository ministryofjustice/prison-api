package net.syscon.util;


import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateTimeConverter {
	public static final String ISO_LOCAL_DATE_FORMAT_PATTERN = "YYYY-MM-DD";

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

		try {
			OffsetDateTime odt = OffsetDateTime.parse(iso8601DateTime);

			ldt = odt.toInstant().atOffset(zoneOffset).toLocalDateTime();
		} catch (DateTimeParseException dtpex) {
			// Perhaps input lacks offset-from-UTC. Try parsing as a local date-time.
			ldt = LocalDateTime.parse(iso8601DateTime);
		}

		return Timestamp.valueOf(ldt);
	}
}
