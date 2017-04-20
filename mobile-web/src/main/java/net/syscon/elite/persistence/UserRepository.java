package net.syscon.elite.persistence;

import net.syscon.elite.model.EliteUser;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public interface UserRepository {
	
	EliteUser findByUsername(String username);
	List<GrantedAuthority> findAuthorities(String username);

}
