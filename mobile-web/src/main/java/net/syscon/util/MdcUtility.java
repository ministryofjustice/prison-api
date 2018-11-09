package net.syscon.util;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MdcUtility {

    public static final String USER_ID_HEADER = "userId";
    public static final String CORRELATION_ID_HEADER = "correlationId";
    public static final String REQUEST_ID = "requestId";
    public static final String REQUEST_DURATION = "duration";
    public static final String RESPONSE_STATUS = "status";
    public static final String SKIP_LOGGING = "skipLogging";

    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static boolean isLoggingAllowed() {
        return !"true".equals(MDC.get(SKIP_LOGGING));
    }
}
