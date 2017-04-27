package net.syscon.elite.persistence;



import java.util.List;

import net.syscon.elite.web.api.model.UserDetails;

public interface UserRepository {
	
	UserDetails findByUsername(String username);
	UserDetails findByStaffId(Long staffId);
	List<String> findRolesByUsername(String username);
}
