package uk.gov.justice.hmpps.prison.util;

import org.springframework.stereotype.Component;

@Component
public class MdcUtility {
    public static final String USER_ID_HEADER = "userId";
    public static final String PROXY_USER = "proxy-user";
    public static final String IP_ADDRESS = "clientIpAddress";
    public static final String REQUEST_URI = "request-url";
    public static final String SUPPRESS_XTAG_EVENTS = "suppress-xtag-events";
}
