package net.syscon.elite.web.config;

import net.syscon.elite.security.ExternalIdAuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.*;
import java.util.stream.Collectors;

public class JWTTokenEnhancer implements TokenEnhancer {
    public static final String ADD_INFO_INTERNAL_USER = "internalUser";
    public static final String ADD_INFO_USER_NAME = "user_name";
    public static final String ADD_INFO_SCOPE = "scope";
    public static final String ADD_INFO_AUTHORITIES = "authorities";

    @Autowired
    private ExternalIdAuthenticationHelper externalIdAuthenticationHelper;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> additionalInfo = new HashMap<>();

        if (authentication.isClientOnly()) {
            if (addUserFromExternalId(authentication, additionalInfo)) {
                additionalInfo.put(ADD_INFO_INTERNAL_USER, Boolean.TRUE);
            } else {
                additionalInfo.put(ADD_INFO_INTERNAL_USER, Boolean.FALSE);
            }
        } else {
            additionalInfo.put(ADD_INFO_INTERNAL_USER, Boolean.TRUE);
        }

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

        return accessToken;
    }

    // Checks for existence of request parameters that define an external user identifier type and identifier.
    // If both identifier type and identifier parameters are present, they will be used to attempt to identify
    // a system user account. If a system user account is not identified, an exception will be thrown to ensure
    // authentication fails. If a system user account is identified, the user id will be added to the token,
    // the token's scope will be 'narrowed' to include 'write' scope and the system user's roles will be added
    // to token authorities.
    private boolean addUserFromExternalId(OAuth2Authentication authentication, Map<String,Object> additionalInfo) {
        // Determine if both user_id_type and user_id request parameters exist.
        OAuth2Request request = authentication.getOAuth2Request();

        Map<String,String> requestParams = request.getRequestParameters();

        // Non-blank user_id_type and user_id to check - delegate to external identifier auth component
        UserDetails userDetails = externalIdAuthenticationHelper.getUserDetails(requestParams);

        if (userDetails != null) {
            additionalInfo.put(ADD_INFO_USER_NAME, userDetails.getUsername());

            // Merge standard user credential scopes with those for client credentials
            Set<String> scope = new HashSet<>(request.getScope());

            scope.add("read");
            scope.add("write");

            additionalInfo.put(ADD_INFO_SCOPE, scope);

            // Merge user authorities with those associated with client credentials
            Set<GrantedAuthority> authorities = new HashSet<>(request.getAuthorities());

            authorities.addAll(userDetails.getAuthorities());

            additionalInfo.put(ADD_INFO_AUTHORITIES,
                    authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
        }

        return !Objects.isNull(userDetails);
    }
}
