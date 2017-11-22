package net.syscon.elite.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configurable
public class ApiAuthenticationProvider extends DaoAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName().toUpperCase();
        String password = authentication.getCredentials().toString();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            logger.debug(String.format("Verified database connection for user: %s", username));
            // Username and credentials are now validated. Must set authentication in security context now
            // so that subsequent user details queries will work.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (final SQLException ex) {
            throw new BadCredentialsException(ex.getMessage(), ex);
        }

        return super.authenticate(authentication);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }
    }
}
