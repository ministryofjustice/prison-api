package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.StaffDetails;
import net.syscon.elite.web.api.model.UserDetails;

import java.util.List;

public interface UserRepository {

	StaffDetails findByStaffId(Long staffId);

	UserDetails findByUsername(String username);

	List<String> findRolesByUsername(String username);

	void updateCurrentLoad(Long staffId, String caseLoadId);
}
