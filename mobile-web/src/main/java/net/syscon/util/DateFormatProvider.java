package net.syscon.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class DateFormatProvider {

    private static final Map<String, ThreadLocal<DateFormat>> THREAD_LOCAL_MAP = new ConcurrentHashMap<>();
    public static final String NORTH_AMERICA_DATE_FORMAT = "MM/dd/yyyy";
    
    private DateFormatProvider() {}


    public static DateFormat get() {
    	return get(NORTH_AMERICA_DATE_FORMAT);
    }

    public static DateFormat get(final String pattern) {
        return get(pattern, TimeZone.getDefault());
    }

    public static DateFormat get(final String pattern, final TimeZone timeZone) {
        final String key = pattern + timeZone.getID();
        ThreadLocal<DateFormat> threadLocal = THREAD_LOCAL_MAP.get(key);
        if (threadLocal == null) {
            threadLocal = new ThreadLocal<>();
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