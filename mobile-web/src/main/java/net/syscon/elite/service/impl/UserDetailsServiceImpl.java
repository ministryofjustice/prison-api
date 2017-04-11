package net.syscon.elite.service.impl;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import net.syscon.elite.model.EliteUser;
import net.syscon.elite.persistence.UserRepository;

public class UserDetailsServiceImpl implements UserDetailsService {

	private UserRepository userRepository;

	@Inject
	public void setUserRepository(final UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public EliteUser loadUserByUsername(final String username) throws UsernameNotFoundException {
		final EliteUser user = this.userRepository.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		}
		return user;
	}

}

