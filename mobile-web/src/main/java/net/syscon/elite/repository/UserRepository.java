package net.syscon.elite.repository;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
	Optional<StaffDetail> findByStaffId(Long staffId);

	Optional<UserDetail> findByUsername(String username);

	List<UserRole> findRolesByUsername(String username);

	void updateWorkingCaseLoad(Long staffId, String caseLoadId);
}
