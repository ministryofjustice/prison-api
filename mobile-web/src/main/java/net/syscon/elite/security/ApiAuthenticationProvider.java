package net.syscon.elite.security;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import static java.lang.String.format;

@Configurable
@Slf4j
public class ApiAuthenticationProvider extends DaoAuthenticationProvider {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private Environment env;

    @Value("${application.caseload.id:NEWB}")
    private String apiCaseloadId;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName().toUpperCase();
        String password = authentication.getCredentials().toString();
        boolean nomisProfile = Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.contains("nomis"));

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            logger.debug(String.format("Verified database connection for user: %s", username));
            // Username and credentials are now validated. Must set authentication in security context now
            // so that subsequent user details queries will work.

            // Check that user has the correct caseload and return access denied if not
            if (nomisProfile && !userService.isUserAssessibleCaseloadAvailable(apiCaseloadId, username)) {
                throw new UnapprovedClientAuthenticationException(format("User does not have access to caseload %s", apiCaseloadId));
            }

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
