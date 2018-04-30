package net.syscon.elite.web.filter;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.UserSecurityUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

import static net.syscon.elite.web.filter.MdcUtility.USER_ID_HEADER;

@Slf4j
@Component
public class UserMdcFilter implements Filter {

    private final UserSecurityUtils userSecurityUtils;

    @Autowired
    public UserMdcFilter(UserSecurityUtils userSecurityUtils) {
        this.userSecurityUtils = userSecurityUtils;
    }

    @Override
    public void init(FilterConfig filterConfig) {
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
    }
}
