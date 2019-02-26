package net.syscon.elite.web.filter;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.AuthenticationFacade;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserMdcFilter(AuthenticationFacade userSecurityUtils) {
        this.userSecurityUtils = userSecurityUtils;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialise - no functionality
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String currentUsername = userSecurityUtils.getCurrentUsername();

        try {
            if (currentUsername != null) {
                MDC.put(USER_ID_HEADER, currentUsername);
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
}
