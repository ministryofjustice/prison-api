package net.syscon.elite.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserSecurityUtils {

	public static String getCurrentUsername() {
		String username;

		Object userPrincipal = getUserPrincipal();

		if (userPrincipal instanceof UserDetails) {
			username = ((UserDetails)userPrincipal).getUsername();
		} else if (userPrincipal instanceof String) {
			username = (String) userPrincipal;
		} else {
			username = null;
		}

		return username;
	}

	public static UserDetailsImpl toUserDetails(Object userPrincipal) {
		UserDetailsImpl userDetails;

		if (userPrincipal instanceof String) {
			userDetails = new UserDetailsImpl((String) userPrincipal, null, Collections.emptyList(), null);
		} else if (userPrincipal instanceof UserDetailsImpl) {
			userDetails = (UserDetailsImpl) userPrincipal;
		} else if (userPrincipal instanceof Map) {
			Map userPrincipalMap = (Map) userPrincipal;

			String username = (String) userPrincipalMap.get("username");
			List authorities = (List) userPrincipalMap.get("authorities");
			Map additionalProperties = (Map) userPrincipalMap.get("additionalProperties");

			if (StringUtils.isNotBlank(username)) {
				userDetails = new UserDetailsImpl(username, null,
						(authorities == null) ? Collections.emptyList() : authorities,
						additionalProperties);
			} else {
				userDetails = null;
			}
		} else {
			userDetails = null;
		}

		return userDetails;
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
