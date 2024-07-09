package uk.gov.justice.hmpps.prison.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.lang.String.format;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.REQUEST_URI;


@Component
@Slf4j
@Order(3)
public class RequestLogFilter implements Filter {
    @Override
    public void init(final FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        final var req = (HttpServletRequest) request;

        try {
            final var requestUri = format("%s %s", req.getMethod(), req.getRequestURI());
            MDC.put(REQUEST_URI, requestUri);

            chain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_URI);
        }
    }
}
