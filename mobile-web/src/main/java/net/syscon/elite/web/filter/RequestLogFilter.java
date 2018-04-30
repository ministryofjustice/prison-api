package net.syscon.elite.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static net.syscon.util.MdcUtility.REQUEST_DURATION;
import static net.syscon.util.MdcUtility.RESPONSE_STATUS;


@Component
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            LocalDateTime start = LocalDateTime.now();
            log.debug("Request: {} {}", request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);

            long duration = Duration.between(start, LocalDateTime.now()).toMillis();
            MDC.put(REQUEST_DURATION, String.valueOf(duration));
            int status = response.getStatus();
            MDC.put(RESPONSE_STATUS, String.valueOf(status));
            log.debug("Response: {} {} - Status {} - Start {}, Duration {} ms", request.getMethod(), request.getRequestURI(), status, start.format(formatter), duration);
        } finally {
            MDC.remove(REQUEST_DURATION);
            MDC.remove(RESPONSE_STATUS);
        }
    }
}
