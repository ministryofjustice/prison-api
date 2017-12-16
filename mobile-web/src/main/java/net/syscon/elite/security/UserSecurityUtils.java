package net.syscon.elite.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
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

		if (userPrincipal instanceof UserDetails) {
			username = ((UserDetails)userPrincipal).getUsername();
		} else if (userPrincipal instanceof String) {
			username = (String) userPrincipal;
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
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return ((!(auth instanceof AnonymousAuthenticationToken) ||
				(auth instanceof PreAuthenticatedAuthenticationToken)) && StringUtils.isNotEmpty(getCurrentUsername()));
	}

	@Override
	public boolean isAnonymousAuthentication() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return auth instanceof AnonymousAuthenticationToken;
	}

	@Override
	public boolean isPreAuthenticatedAuthenticationToken() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return auth instanceof PreAuthenticatedAuthenticationToken;
	}

	private Object getUserPrincipal() {
		Object userPrincipal;

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			userPrincipal = null;
		} else {
			userPrincipal = auth.getPrincipal();
		}

		return userPrincipal;
	}
}
