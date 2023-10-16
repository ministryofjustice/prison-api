package uk.gov.justice.hmpps.prison.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.exception.MissingRoleCheckException;
import uk.gov.justice.hmpps.prison.web.config.AuthAwareAuthenticationToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class AuthenticationFacade {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getCurrentUsername() {
        final String username;

        final var userPrincipal = getUserPrincipal();

        if (userPrincipal instanceof String) {
            username = (String) userPrincipal;
        } else if (userPrincipal instanceof UserDetails) {
            username = ((UserDetails) userPrincipal).getUsername();
        } else if (userPrincipal instanceof Map) {
            final var userPrincipalMap = (Map) userPrincipal;
            username = (String) userPrincipalMap.get("username");
        } else {
            username = null;
        }

        return username;
    }

    public Collection<? extends GrantedAuthority> getCurrentRoles() {
        return getAuthentication() != null ? getAuthentication().getAuthorities() : null;
    }

    public boolean isClientOnly() {
        final var authentication = getAuthentication();
        if (!(authentication instanceof AuthAwareAuthenticationToken)) return false;

        final var token = (AuthAwareAuthenticationToken) authentication;
        return token.isClientOnly();
    }

    public AuthSource getAuthenticationSource() {
        final var auth = getAuthentication();
        return Optional.ofNullable(auth).
                filter(AuthAwareAuthenticationToken.class::isInstance).
                map(AuthAwareAuthenticationToken.class::cast).
                filter(AbstractAuthenticationToken::isAuthenticated).
                map(AuthAwareAuthenticationToken::getAuthSource).
                orElse(AuthSource.NONE);
    }

    public static boolean hasRoles(final String... allowedRoles) {
        return hasMatchingRole(getRoles(allowedRoles), SecurityContextHolder.getContext().getAuthentication());
    }

    public boolean isOverrideRole(final String... overrideRoles) {
        return hasMatchingRole(getRoles(overrideRoles), getAuthentication());
    }

    private static boolean hasMatchingRole(final List<String> roles, final Authentication authentication) {
        if (roles.isEmpty()) throw new MissingRoleCheckException("No role supplied to check against authentication");
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> roles.contains(RegExUtils.replaceFirst(a.getAuthority(), "ROLE_", "")));
    }

    private Object getUserPrincipal() {
        final var auth = getAuthentication();
        return auth != null ? auth.getPrincipal() : null;
    }

    private static List<String> getRoles(String... roles){
        return Arrays.stream(roles).map(r -> RegExUtils.replaceFirst(r, "ROLE_", "")).toList();
    }
}
