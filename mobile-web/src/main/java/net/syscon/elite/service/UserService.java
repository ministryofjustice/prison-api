package net.syscon.elite.service;

import net.syscon.elite.web.api.model.CaseLoad;
import net.syscon.elite.web.api.model.UserDetails;

import java.util.List;

public interface UserService {

	UserDetails getUserByUsername(String username);

	CaseLoad getActiveCaseLoad(String username);

	List<CaseLoad> getCaseLoads(String username);

	void setActiveCaseLoad(String username, String caseLoadId);
}
