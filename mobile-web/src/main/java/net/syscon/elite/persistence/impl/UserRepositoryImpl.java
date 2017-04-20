package net.syscon.elite.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import net.syscon.elite.model.EliteUser;
import net.syscon.elite.persistence.UserRepository;

@Repository
public class UserRepositoryImpl implements UserRepository {

	@Override
	public EliteUser findByUsername(final String username) {
		final EliteUser user = new EliteUser();
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
