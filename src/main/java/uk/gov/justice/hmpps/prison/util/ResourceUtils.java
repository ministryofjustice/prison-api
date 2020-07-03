package uk.gov.justice.hmpps.prison.util;

public enum ResourceUtils {
    INSTANCE;

    public static <T> T nvl(final T assertNullVal, final T defaultVal) {
        return assertNullVal != null ? assertNullVal : defaultVal;
    }
}
