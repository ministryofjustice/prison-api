package uk.gov.justice.hmpps.prison.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.service.UserService;
import uk.gov.justice.hmpps.prison.util.ProfileUtil;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service("userDetailsService")
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    private final UserService userService;
    private final String apiCaseloadId;

    @Autowired
    private Environment env;

    public UserDetailsServiceImpl(final UserService userService,
                                  @Value("${application.caseload.id:NEWB}") final String apiCaseloadId) {
        this.userService = userService;
        this.apiCaseloadId = apiCaseloadId;
    }

    @Override
    @Cacheable(value = "loadUserByUsername", unless = "#result == null")
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final var nomisProfile = ProfileUtil.isNomisProfile(env);

        final var roles = userService.getRolesByUsername(username, false);

        if (nomisProfile && !userService.isUserAccessibleCaseloadAvailable(apiCaseloadId, username)) {
            throw new InsufficientAuthenticationException(format("User does not have access to caseload %s", apiCaseloadId));
        }

        final Set<GrantedAuthority> authorities = roles.stream()
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + StringUtils.upperCase(StringUtils.replaceAll(role.getRoleCode(), "-", "_"))))
                .collect(Collectors.toSet());

        return new UserDetailsImpl(username, null, authorities);
    }

    @Override
    public UserDetails loadUserDetails(final PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        return loadUserByUsername(token.getName());
    }
}
