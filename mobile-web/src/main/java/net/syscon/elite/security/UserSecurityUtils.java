package net.syscon.elite.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserSecurityUtils implements AuthenticationFacade {

	@Override
	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	@Override
	public String getCurrentUsername() {
		String username;

		Object userPrincipal = getUserPrincipal();

	    if (userPrincipal instanceof String) {
			username = (String) userPrincipal;
		} else if (userPrincipal instanceof UserDetails) {
			username = ((UserDetails)userPrincipal).getUsername();
		} else if (userPrincipal instanceof Map) {
			Map userPrincipalMap = (Map) userPrincipal;
			username = (String) userPrincipalMap.get("username");
		} else {
			username = null;
		}

		return username;
	}

	@Override
	public boolean isIdentifiedAuthentication() {
		Authentication auth = getAuthentication();
		return auth != null && (
				(auth instanceof OAuth2Authentication && auth.isAuthenticated() && !((OAuth2Authentication) auth).isClientOnly())
		);
	}

	private Object getUserPrincipal() {
		Object userPrincipal = null;

		final Authentication auth = getAuthentication();

		if (auth != null) {
			userPrincipal = auth.getPrincipal();
		}
		return userPrincipal;
	}
}
