package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.web.api.model.CaseLoad;
import net.syscon.elite.web.api.model.UserDetails;

public interface UserService {
	
	UserDetails getUserByStaffId(Long staffId);
	UserDetails getUserByUsername(String username);
	CaseLoad getActiveCaseLoad(Long staffId);
	List<CaseLoad> getCaseLoads(Long staffId);
	void setActiveCaseLoad(Long staffId, String caseLoadId);

}
