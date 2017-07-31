package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.StaffDetails;
import net.syscon.elite.web.api.model.UserDetails;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

	Optional<StaffDetails> findByStaffId(Long staffId);

	Optional<UserDetails> findByUsername(String username);

	List<String> findRolesByUsername(String username);

	void updateCurrentLoad(Long staffId, String caseLoadId);
}
