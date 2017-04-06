package net.syscon.elite.persistence;

import net.syscon.elite.model.EliteUser;

public interface UserRepository {
	
	EliteUser findByUsername(String username);
	
}
