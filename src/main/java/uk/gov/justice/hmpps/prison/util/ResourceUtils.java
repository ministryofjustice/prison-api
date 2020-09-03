package uk.gov.justice.hmpps.prison.util;

import org.apache.commons.lang3.StringUtils;

public enum ResourceUtils {
    INSTANCE;

    public static <T> T nvl(final T assertNullVal, final T defaultVal) {
        return assertNullVal != null ? assertNullVal : defaultVal;
    }

    public static String getUniqueClientId(final String clientName, final String clientUniqueRef) {
        if (StringUtils.isBlank(clientUniqueRef)) {
            return null;
        }
        return StringUtils.isNotBlank(clientName) ? clientName + "-" + clientUniqueRef : clientUniqueRef;
    }
}
