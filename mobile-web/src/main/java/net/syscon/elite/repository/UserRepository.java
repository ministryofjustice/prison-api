package net.syscon.elite.repository;

import net.syscon.elite.api.model.StaffUserRole;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
	Optional<UserDetail> findByUsername(String username);

	List<UserRole> findRolesByUsername(String username, String query);

	List<UserRole> findAccessRolesByUsernameAndCaseload(String username, String caseload);

	void updateWorkingCaseLoad(Long staffId, String caseLoadId);

	Optional<UserDetail> findByStaffIdAndStaffUserType(Long staffId, String userType);

	Optional<Long> getRoleIdForCode(String roleCode);

	boolean isUserAssessibleCaseloadAvailable(String caseload, String username);

	void addUserAssessibleCaseload(String caseload, String username);

	List<StaffUserRole> getAllStaffRolesForCaseload(String caseload, String roleCode);

	boolean isRoleAssigned(String username, String caseload, long roleId);

	void addRole(String username, String caseload, Long roleId);

	void removeRole(String username, String caseload, Long roleId);

	List<UserDetail> findAllUsersWithCaseload(String caseloadId);

    Page<UserDetail> findUsersByCaseload(String agencyId, String accessRole, String nameFilter, PageRequest pageRequest);

    Page<UserDetail> findLocalAdministratorUsersByCaseload(String agencyId, String accessRole, String nameFilter, PageRequest pageRequest);
}
