package uk.gov.justice.hmpps.prison.web.filter;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.util.IpAddressHelper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import static uk.gov.justice.hmpps.prison.util.MdcUtility.IP_ADDRESS;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.USER_ID_HEADER;

@Slf4j
@Component
@Order(1)
@AllArgsConstructor
public class UserMdcFilter implements Filter {
    private final HmppsAuthenticationHolder hmppsAuthenticationHolder;
    private final TelemetryClient telemetryClient;

    @Override
    public void init(final FilterConfig filterConfig) {
        // Initialise - no functionality
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        final var authenticationOrNull = hmppsAuthenticationHolder.getAuthenticationOrNull();

        try {
            if (authenticationOrNull != null) {
                MDC.put(USER_ID_HEADER, authenticationOrNull.getPrincipal());
                telemetryClient.getContext().getUser().setId(authenticationOrNull.getPrincipal());
            }
            final var ip = IpAddressHelper.retrieveIpFromRemoteAddr((HttpServletRequest) request);
            MDC.put(IP_ADDRESS, ip);
            telemetryClient.getContext().getLocation().setIp(ip);

            chain.doFilter(request, response);
        } finally {
            if (authenticationOrNull != null) {
                MDC.remove(USER_ID_HEADER);
            }
            MDC.remove(IP_ADDRESS);
        }
    }

    @Override
    public void destroy() {
        // Destroy - no functionality
    }
}
