package net.syscon.elite.web.filter;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.AuthenticationFacade;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

import static net.syscon.util.MdcUtility.USER_ID_HEADER;

@Slf4j
@Component
@Order(1)
public class UserMdcFilter implements Filter {

    private final AuthenticationFacade userSecurityUtils;
    private final TelemetryClient telemetryClient;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Autowired
    public UserMdcFilter(final AuthenticationFacade userSecurityUtils, TelemetryClient telemetryClient) {
        this.userSecurityUtils = userSecurityUtils;
        this.telemetryClient = telemetryClient;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        // Initialise - no functionality
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        final var currentUsername = userSecurityUtils.getCurrentUsername();

        try {
            if (currentUsername != null) {
                MDC.put(USER_ID_HEADER, currentUsername);
                telemetryClient.getContext().getUser().setId(currentUsername);
                telemetryClient.getContext().getComponent().setVersion(getVersion());
            }
            chain.doFilter(request, response);
        } finally {
            if (currentUsername != null) {
                MDC.remove(USER_ID_HEADER);
            }
        }
    }

    @Override
    public void destroy() {
        // Destroy - no functionality
    }

    private String getVersion() {
        return buildProperties == null ? "N/A" : buildProperties.getVersion();
    }

}
