package net.syscon.elite.web.filter;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MdcUtility {

    static final String USER_ID_HEADER = "userId";
    static final String CORRELATION_ID_HEADER = "correlationId";
    public static final String REQUEST_DURATION = "duration";

    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

}
