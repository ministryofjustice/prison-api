package net.syscon.elite.security;

import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final net.syscon.elite.web.api.model.UserDetails userDetails = userRepository.findByUsername(username).orElseThrow(new EntityNotFoundException(username));
		List<String> roles = userRepository.findRolesByUsername(username);

		Set<GrantedAuthority> authorities = roles.stream()
				.filter(Objects::nonNull)
				.map(name -> new SimpleGrantedAuthority(name.replace('-', '_')))
				.collect(Collectors.toSet());

		return new UserDetailsImpl(username, null, authorities, userDetails.getAdditionalProperties());
	}
}
