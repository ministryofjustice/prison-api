package uk.gov.justice.hmpps.prison.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName.MERGE;
import static uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName.PRISON_API;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.NOMIS_CONTEXT;


@Component
@Slf4j
public class NomisContextFilter implements Filter {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    private final Pattern excludeUriRegex;

    @Autowired
    public NomisContextFilter(@Value("${logging.uris.exclude.regex}") final String excludeUris) {
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

        MDC.put(NOMIS_CONTEXT, PRISON_API.name());
        final var req = (HttpServletRequest) request;
        if ("true".equals(req.getHeader("no-xtag-events"))) {
            MDC.put(NOMIS_CONTEXT, MERGE.name());
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
