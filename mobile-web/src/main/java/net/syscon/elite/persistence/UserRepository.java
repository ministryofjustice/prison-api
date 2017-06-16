package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.UserDetails;

import java.util.List;

public interface UserRepository {
	
	UserDetails findByUsername(String username);
	List<UserDetails> findByStaffId(Long staffId);
	List<String> findRolesByUsername(String username);
	void updateCurrentLoad(final Long staffId, final String caseLoadId);
}
