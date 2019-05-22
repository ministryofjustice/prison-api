package net.syscon.elite.web.filter;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import net.syscon.util.MdcUtility;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

import static net.syscon.util.MdcUtility.CORRELATION_ID_HEADER;

@Slf4j
@Component
@Order(2)
public class CorrelationHeaderFilter implements Filter {

    private final MdcUtility mdcUtility;
    private final TelemetryClient telemetryClient;

    @Autowired
    public CorrelationHeaderFilter(final MdcUtility mdcUtility, TelemetryClient telemetryClient) {
        this.mdcUtility = mdcUtility;
        this.telemetryClient = telemetryClient;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        // Initialise - no functionality
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        final var correlationIdOptional = Optional.ofNullable(((HttpServletRequest) request).getHeader(CORRELATION_ID_HEADER));

        try {
            var correlationId = correlationIdOptional.orElseGet(mdcUtility::generateCorrelationId);
            MDC.put(CORRELATION_ID_HEADER, correlationId);
            telemetryClient.getContext().getProperties().putIfAbsent(CORRELATION_ID_HEADER, correlationId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_HEADER);
        }
    }

    @Override
    public void destroy() {
        // destroy - no functionality
    }

}
