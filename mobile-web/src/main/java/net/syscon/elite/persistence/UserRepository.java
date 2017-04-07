package net.syscon.elite.persistence;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import net.syscon.elite.model.EliteUser;

public interface UserRepository {
	
	EliteUser findByUsername(String username);
	List<GrantedAuthority> findAuthorities(String username);
	
}
