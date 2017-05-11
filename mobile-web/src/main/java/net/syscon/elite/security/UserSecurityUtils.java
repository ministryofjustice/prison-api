package net.syscon.elite.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserSecurityUtils {


	public static String getCurrentUsername() {
		String username = null;
		Object userPrincipal = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			userPrincipal = auth.getPrincipal();
			if (userPrincipal instanceof UserDetails) {
				username = ((UserDetails)userPrincipal).getUsername();
			} else if (userPrincipal instanceof String) {
				username = (String) userPrincipal;
			}
		}
		return username;
	}

}
