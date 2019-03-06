package net.syscon.util;

public enum ResourceUtils {
    INSTANCE;

    public static <T> T nvl(final T assertNullVal, final T defaultVal) {
        return assertNullVal != null ? assertNullVal : defaultVal;
    }
}
