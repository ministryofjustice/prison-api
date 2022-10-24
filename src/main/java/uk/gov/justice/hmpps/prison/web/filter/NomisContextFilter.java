package uk.gov.justice.hmpps.prison.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName.MERGE;
import static uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName.PRISON_API;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.NOMIS_CONTEXT;


@Component
@Slf4j
public class NomisContextFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        MDC.put(NOMIS_CONTEXT, PRISON_API.name());
        final var req = (HttpServletRequest) request;
        if ("true".equals(req.getHeader("no-event-propagation"))) {
            log.info("no-event-propagation header detected, using MERGE context.");
            MDC.put(NOMIS_CONTEXT, MERGE.name());
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
