package net.syscon.elite.service;

import net.syscon.elite.web.api.model.CaseLoad;
import net.syscon.elite.web.api.model.UserDetails;

import java.util.List;

public interface UserService {
	
	List<UserDetails> getUserByStaffId(Long staffId);
	UserDetails getUserByUsername(String username);
	CaseLoad getActiveCaseLoad(final String username);
	List<CaseLoad> getCaseLoads(Long staffId);
	void setActiveCaseLoad(Long staffId, String caseLoadId);

}
