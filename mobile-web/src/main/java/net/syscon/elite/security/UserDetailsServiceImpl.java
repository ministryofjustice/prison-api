package net.syscon.elite.security;

import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service("userDetailsService")
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
	private final UserService userService;
	private final String apiCaseloadId;

	public UserDetailsServiceImpl(UserService userService,
								  @Value("${application.caseload.id:NEWB}") String apiCaseloadId) {
		this.userService = userService;
		this.apiCaseloadId = apiCaseloadId;
	}

	@Override
	@Cacheable("loadUserByUsername")
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final UserDetail userDetail = userService.getUserByUsername(username);
		List<UserRole> roles = userService.getRolesByUsername(username, false);

		if (!userService.isUserAssessibleCaseloadAvailable(apiCaseloadId, username)) {
			throw new UnapprovedClientAuthenticationException(format("User does not have access to caseload %s", apiCaseloadId));
		}

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
