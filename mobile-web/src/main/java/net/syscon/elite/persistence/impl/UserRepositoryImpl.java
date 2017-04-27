package net.syscon.elite.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.web.api.model.UserDetails;

@Repository
public class UserRepositoryImpl implements UserRepository {

	@Override
	public UserDetails findByUsername(final String username) {
		final UserDetails user = new UserDetails();
		user.setUsername(username);
		return user;
	}

	@Override
	public List<GrantedAuthority> findAuthorities(final String username) {
		final List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		return authorities;
	}
	

}
