package net.syscon.elite.security;

import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Intended for use with OAuth2 client credentials (system-to-system) authentication
 * to supplement access token with system user identified by an external user identifier
 * and identifier type (provided as authentication request parameters).
 */
@Component
public class ExternalIdAuthenticationHelper {
    public static final String REQUEST_PARAM_USER_ID_TYPE = "user_id_type";
    public static final String REQUEST_PARAM_USER_ID = "user_id";
    public static final String REQUEST_PARAM_USER_NAME = "username";

    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public ExternalIdAuthenticationHelper(UserService userService, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    public UserDetails getUserDetails(Map<String, String> requestParameters) {
        UserDetails userDetails = null;

        if (requestParameters.containsKey(REQUEST_PARAM_USER_ID_TYPE) &&
                requestParameters.containsKey(REQUEST_PARAM_USER_ID)) {
            // Extract values - if either are empty/null, throw auth failed exception
            String userIdType = requestParameters.get(REQUEST_PARAM_USER_ID_TYPE);
            String userId = requestParameters.get(REQUEST_PARAM_USER_ID);

            if (StringUtils.isBlank(userIdType) || StringUtils.isBlank(userId)) {
                throw new OAuth2AccessDeniedException("Invalid external user identifier details.");
            }

            UserDetail userDetail;

            try {
                userDetail = userService.getUserByExternalIdentifier(userIdType, userId, true);
            } catch (EntityNotFoundException ex) {
                throw new OAuth2AccessDeniedException("No user found matching external user identifier details.");
            }
            // Get full user details, with authorities, etc.
            userDetails = userDetailsService.loadUserByUsername(userDetail.getUsername());
        } else if (requestParameters.containsKey(REQUEST_PARAM_USER_NAME)) {
            String username = requestParameters.get(REQUEST_PARAM_USER_NAME);

            if (StringUtils.isBlank(username)) {
                throw new OAuth2AccessDeniedException("Invalid username identifier details.");
            }

            UserDetail userDetail;

            try {
                userDetail = userService.getUserByUsername(username);
            } catch (EntityNotFoundException ex) {
                throw new OAuth2AccessDeniedException("No user found matching username.");
            }
            // Get full user details, with authorities, etc.
            userDetails = userDetailsService.loadUserByUsername(userDetail.getUsername());
        }

        return userDetails;
    }
}
