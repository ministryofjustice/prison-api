package net.syscon.elite.repository;


import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.UserDetail;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

	Optional<StaffDetail> findByStaffId(Long staffId);

	Optional<UserDetail> findByUsername(String username);

	List<String> findRolesByUsername(String username);

	void updateCurrentLoad(Long staffId, String caseLoadId);
}
