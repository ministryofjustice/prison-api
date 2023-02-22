package uk.gov.justice.hmpps.prison.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import static uk.gov.justice.hmpps.prison.util.MdcUtility.SUPPRESS_XTAG_EVENTS;


@Component
@Slf4j
public class EventPropagationFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        try {
            var suppress = "false";
            final var req = (HttpServletRequest) request;
            if ("true".equals(req.getHeader("no-event-propagation"))) {
                log.info("no-event-propagation header detected, flagging XTag events for suppression.");
                suppress = "true";
            }
            MDC.put(SUPPRESS_XTAG_EVENTS, suppress);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(SUPPRESS_XTAG_EVENTS);
        }
    }

    @Override
    public void destroy() {

    }
}
