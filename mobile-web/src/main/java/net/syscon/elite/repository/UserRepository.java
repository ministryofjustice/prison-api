package net.syscon.elite.repository;

import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
	Optional<UserDetail> findByUsername(String username);

	List<UserRole> findRolesByUsername(String username, String query);

	void updateWorkingCaseLoad(Long staffId, String caseLoadId);

	Optional<UserDetail> findByStaffIdAndStaffUserType(Long staffId, String userType);
}
