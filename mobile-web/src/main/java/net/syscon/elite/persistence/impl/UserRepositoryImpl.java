package net.syscon.elite.persistence.impl;

import org.springframework.stereotype.Repository;

import net.syscon.elite.model.EliteUser;
import net.syscon.elite.persistence.UserRepository;

@Repository
public class UserRepositoryImpl implements UserRepository {

	@Override
	public EliteUser findByUsername(final String username) {
		final EliteUser user = new EliteUser();
		user.setUsername(username);
		user.setEnabled(true);
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		return user;
	}
	

}
