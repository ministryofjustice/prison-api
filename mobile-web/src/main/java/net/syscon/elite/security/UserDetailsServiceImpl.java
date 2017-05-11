package net.syscon.elite.security;

import net.syscon.elite.persistence.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

	private UserRepository userRepository;

	@Inject
	public void setUserRepository(final UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final net.syscon.elite.web.api.model.UserDetails user = this.userRepository.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		}
		List<String> roles = userRepository.findRolesByUsername(username);
		Set<GrantedAuthority> authorities = roles.stream()
				.filter(name -> name != null)
				.map(name -> new SimpleGrantedAuthority(name))
				.collect(Collectors.toSet());
		return new UserDetailsImpl(username, null, authorities);
	}


}
