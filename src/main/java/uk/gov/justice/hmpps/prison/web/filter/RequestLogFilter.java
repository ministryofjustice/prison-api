package uk.gov.justice.hmpps.prison.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.REQUEST_DURATION;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.REQUEST_URI;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.RESPONSE_STATUS;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.SKIP_LOGGING;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.isLoggingAllowed;


@Component
@Slf4j
@Order(3)
public class RequestLogFilter implements Filter {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    private final Pattern excludeUriRegex;

    @Autowired
    public RequestLogFilter(@Value("${logging.uris.exclude.regex}") final String excludeUris) {
        excludeUriRegex = Pattern.compile(excludeUris);
    }

    @Override
    public void init(final FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        final var req = (HttpServletRequest) request;
        final var res = (HttpServletResponse) response;
        if (excludeUriRegex.matcher(req.getRequestURI()).matches()) {
            MDC.put(SKIP_LOGGING, "true");
        }

        try {
            final var start = LocalDateTime.now();

            final var requestUri = format("%s %s", req.getMethod(), req.getRequestURI());
            MDC.put(REQUEST_URI, requestUri);

            if (log.isTraceEnabled() && isLoggingAllowed()) {
                log.trace("Request: {}", requestUri);
            }

            chain.doFilter(request, response);

            final var duration = Duration.between(start, LocalDateTime.now()).toMillis();
            MDC.put(REQUEST_DURATION, String.valueOf(duration));
            final var status = res.getStatus();
            MDC.put(RESPONSE_STATUS, String.valueOf(status));
            if (log.isTraceEnabled() && isLoggingAllowed()) {
                log.trace("Response: {} - Status {} - Start {}, Duration {} ms", requestUri, status, start.format(formatter), duration);
            }
        } finally {
            MDC.remove(REQUEST_DURATION);
            MDC.remove(RESPONSE_STATUS);
            MDC.remove(SKIP_LOGGING);
            MDC.remove(REQUEST_URI);
        }
    }

    @Override
    public void destroy() {

    }
}
