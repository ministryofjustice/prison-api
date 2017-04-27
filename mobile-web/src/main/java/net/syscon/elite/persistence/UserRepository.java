package net.syscon.elite.persistence;


import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import net.syscon.elite.web.api.model.UserDetails;

public interface UserRepository {
	
	UserDetails findByUsername(String username);
	List<GrantedAuthority> findAuthorities(String username);

}
