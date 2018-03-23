package net.syscon.elite.repository;

import net.syscon.elite.api.model.StaffUserRole;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
	Optional<UserDetail> findByUsername(String username);

	List<UserRole> findRolesByUsername(String username, String query);

	void updateWorkingCaseLoad(Long staffId, String caseLoadId);

	Optional<UserDetail> findByStaffIdAndStaffUserType(Long staffId, String userType);

	Optional<Long> getRoleIdForCode(String roleCode);

	boolean isUserAssessibleCaseloadAvailable(String caseload, String username);

	void addUserAssessibleCaseload(String caseload, String username);

	List<StaffUserRole> getAllStaffRolesForCaseload(String caseload, String roleCode);

	void addRole(String username, String caseload, Long roleId);

	void removeRole(String username, String caseload, Long roleId);
}
