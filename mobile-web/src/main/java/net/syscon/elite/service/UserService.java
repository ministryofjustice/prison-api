package net.syscon.elite.service;

import net.syscon.elite.v2.api.model.CaseLoad;
import net.syscon.elite.v2.api.model.StaffDetail;
import net.syscon.elite.v2.api.model.UserDetail;

import java.util.List;

public interface UserService {

	StaffDetail getUserByStaffId(Long staffId);

	UserDetail getUserByUsername(String username);

	CaseLoad getActiveCaseLoad(String username);

	List<CaseLoad> getCaseLoads(String username);

	void setActiveCaseLoad(String username, String caseLoadId);
}
