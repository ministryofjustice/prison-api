package net.syscon.elite.security;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    Authentication getAuthentication();

    String getCurrentUsername();

    boolean isAnonymousAuthentication();

    boolean isPreAuthenticatedAuthenticationToken();

    boolean isIdentifiedAuthentication();
}
