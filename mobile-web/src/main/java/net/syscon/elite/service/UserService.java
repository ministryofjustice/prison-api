package net.syscon.elite.service;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;

import java.util.List;
import java.util.Set;

public interface UserService {

	StaffDetail getUserByStaffId(Long staffId);

	UserDetail getUserByUsername(String username);

	List<CaseLoad> getCaseLoads(String username);

	Set<String> getCaseLoadIds(String username);

	void setActiveCaseLoad(String username, String caseLoadId);

	List<UserRole> getRolesByUsername(String username);

	List<UserRole> getApiRolesByUsername(String username);
}
