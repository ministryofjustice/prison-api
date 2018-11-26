package net.syscon.util;

public enum ResourceUtils {
    INSTANCE;

    public static <T> T nvl(T assertNullVal, T defaultVal) {
        return assertNullVal != null ? assertNullVal : defaultVal;
    }
}
