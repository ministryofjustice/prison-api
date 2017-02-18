package net.syscon.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class DateFormatProvider {

    private static final Map<String, ThreadLocal<DateFormat>> THREAD_LOCAL_MAP = new ConcurrentHashMap<String, ThreadLocal<DateFormat>>();

    public static final String NORTH_AMERICA_DATE_FORMAT = "MM/dd/yyyy";


    public static DateFormat get() {
		final StringBuffer buffer = new StringBuffer();
        final Calendar date = Calendar.getInstance();
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        final FieldPosition yearPosition = new FieldPosition(DateFormat.YEAR_FIELD);
        final StringBuffer pattern = dateFormat.format(date.getTime(), buffer, yearPosition);
        pattern.replace(yearPosition.getBeginIndex(), yearPosition.getEndIndex(), String.valueOf(date.get(Calendar.YEAR)));
        return get(pattern.toString(), TimeZone.getDefault());
    }

    public static DateFormat get(String pattern) {
        return get(pattern, TimeZone.getDefault());
    }

    public static DateFormat get(String pattern, TimeZone timeZone) {
        final String key = pattern + timeZone.getID();
        ThreadLocal<DateFormat> threadLocal = THREAD_LOCAL_MAP.get(key);
        if (threadLocal == null) {
            threadLocal = new ThreadLocal<DateFormat>();
            THREAD_LOCAL_MAP.put(key, threadLocal);
        }
        DateFormat dateFormat = threadLocal.get();
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(timeZone);
            threadLocal.set(dateFormat);
        }
        return dateFormat;
    }

}