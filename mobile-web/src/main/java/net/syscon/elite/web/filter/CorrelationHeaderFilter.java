package net.syscon.elite.web.filter;

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

    @Autowired
    public CorrelationHeaderFilter(MdcUtility mdcUtility) {
        this.mdcUtility = mdcUtility;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialise - no functionality
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Optional<String> correlationIdOptional = Optional.ofNullable(((HttpServletRequest)request).getHeader(CORRELATION_ID_HEADER));

        try {
            MDC.put(CORRELATION_ID_HEADER, correlationIdOptional.orElseGet(mdcUtility::generateCorrelationId));
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
