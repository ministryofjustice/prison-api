package net.syscon.util;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeConverter {


	public static Date toDate(LocalDateTime localDateTime) {
		Date result = null;
		if (localDateTime != null) {
			result = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		}
		return result;
	}

	public static LocalDateTime toLocalDateTime(Date date) {
		LocalDateTime result = null;
		if (date != null) {
			result = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		}
		return result;
	}


}
