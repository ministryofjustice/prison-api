package net.syscon.elite.web.filter;

import lombok.extern.slf4j.Slf4j;
import net.syscon.util.MdcUtility;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static net.syscon.util.MdcUtility.*;


@Component
@Slf4j
@Order(3)
public class RequestLogFilter implements Filter {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    private final MdcUtility mdcUtility;

    private final Pattern excludeUriRegex;

    @Autowired
    public RequestLogFilter(MdcUtility mdcUtility, @Value("${logging.uris.exclude.regex}") String excludeUris) {
        this.mdcUtility = mdcUtility;
        excludeUriRegex = Pattern.compile(excludeUris);
    }
    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (excludeUriRegex.matcher(req.getRequestURI()).matches()) {
            MDC.put(SKIP_LOGGING, "true");
        }

        try {
            LocalDateTime start = LocalDateTime.now();
            MDC.put(REQUEST_ID, mdcUtility.generateCorrelationId());
            if (isLoggingAllowed()) {
                log.info("Request: {} {}", req.getMethod(), req.getRequestURI());
            }

            chain.doFilter(request, response);

            long duration = Duration.between(start, LocalDateTime.now()).toMillis();
            MDC.put(REQUEST_DURATION, String.valueOf(duration));
            int status = res.getStatus();
            MDC.put(RESPONSE_STATUS, String.valueOf(status));
            if (isLoggingAllowed()) {
                log.info("Response: {} {} - Status {} - Start {}, Duration {} ms", req.getMethod(), req.getRequestURI(), status, start.format(formatter), duration);
            }
        } finally {
            MDC.remove(REQUEST_DURATION);
            MDC.remove(RESPONSE_STATUS);
            MDC.remove(REQUEST_ID);
            MDC.remove(SKIP_LOGGING);
        }
    }

    @Override
    public void destroy() {

    }
}
