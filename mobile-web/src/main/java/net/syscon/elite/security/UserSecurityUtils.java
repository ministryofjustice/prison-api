package net.syscon.elite.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Map;

public class UserSecurityUtils {

	public static String getCurrentUsername() {
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

	public static boolean isAnonymousAuthentication() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return auth instanceof AnonymousAuthenticationToken;
	}

	public static boolean isPreAuthenticatedAuthenticationToken() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return auth instanceof PreAuthenticatedAuthenticationToken;
	}


	private static Object getUserPrincipal() {
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
