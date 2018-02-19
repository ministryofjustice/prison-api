package net.syscon.elite.security;

import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("userDetailsService")
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
	private final UserService userService;

	public UserDetailsServiceImpl(UserService userService) {
		this.userService = userService;
	}

	@Override
	@Cacheable("loadUserByUsername")
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final UserDetail userDetail = userService.getUserByUsername(username);
		List<UserRole> roles = userService.getApiRolesByUsername(username);

		Set<GrantedAuthority> authorities = roles.stream()
				.filter(Objects::nonNull)
				.map(role -> new SimpleGrantedAuthority("ROLE_" + StringUtils.upperCase(StringUtils.replaceAll(role.getRoleCode(),"-", "_"))))
				.collect(Collectors.toSet());

		return new UserDetailsImpl(username, null, authorities, userDetail.getAdditionalProperties());
	}

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        return loadUserByUsername(token.getName());
    }
}
