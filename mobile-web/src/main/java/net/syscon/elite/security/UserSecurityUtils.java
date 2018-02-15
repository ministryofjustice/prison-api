package net.syscon.elite.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserSecurityUtils implements AuthenticationFacade {

	@Value("${application.client.username}")
	private String clientLoginUsername;

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
		Authentication auth = getAuthentication();
		return auth != null && (
				(auth instanceof UsernamePasswordAuthenticationToken)
				||
				(auth instanceof OAuth2Authentication && auth.isAuthenticated())
		);
	}

	private Object getUserPrincipal() {
		Object userPrincipal = null;

		final Authentication auth = getAuthentication();

		if (auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
			// This is a client auth
			userPrincipal = clientLoginUsername;
		} else {
			if (auth instanceof OAuth2Authentication && ((OAuth2Authentication) auth).isClientOnly()) {
				userPrincipal = clientLoginUsername;
			}
		}

		if (userPrincipal == null && auth != null) {
			userPrincipal = auth.getPrincipal();
		}
		return userPrincipal;
	}
}
