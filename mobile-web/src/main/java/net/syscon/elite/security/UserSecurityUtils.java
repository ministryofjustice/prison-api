package net.syscon.elite.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public static boolean isAnonymousAuthentication() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return auth instanceof AnonymousAuthenticationToken;
	}

	@SuppressWarnings("unchecked")
	public static UserDetailsImpl toUserDetails(Object userPrincipal) {
		UserDetailsImpl userDetails;

		if (userPrincipal instanceof String) {
			userDetails = new UserDetailsImpl((String) userPrincipal, null, Collections.emptyList(), null);

		} else if (userPrincipal instanceof UserPrincipalForToken) {
			final Set<GrantedAuthority> authorities = getGrantedAuthorities(((UserPrincipalForToken) userPrincipal).getRoles().stream());
			userDetails = new UserDetailsImpl(((UserPrincipalForToken) userPrincipal).getUsername(), null, authorities, null);

		} else if (userPrincipal instanceof Map) {
			Map userPrincipalMap = (Map) userPrincipal;
			final String username = (String) userPrincipalMap.get("username");
			if (StringUtils.isNotBlank(username)) {
				final Set<GrantedAuthority> authorities = getGrantedAuthorities(((List<String>) userPrincipalMap.get("roles")).stream());
				userDetails = new UserDetailsImpl(username, null, authorities, null);
			} else {
				userDetails = null;
			}
		} else {
			userDetails = null;
		}

		return userDetails;
	}

	private static Set<GrantedAuthority> getGrantedAuthorities(Stream<String> roles) {
		return roles.filter(Objects::nonNull)
				.map(value -> new SimpleGrantedAuthority("ROLE_" + value))
				.collect(Collectors.toSet());
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
