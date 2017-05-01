package net.syscon.elite.persistence;

import java.util.List;

import net.syscon.elite.web.api.model.CaseLoad;

public interface CaseLoadRepository {
	CaseLoad find(Long caseLoadId);
	List<CaseLoad> findCaseLoadsByStaffId(Long staffId);
	int updateCurrentLoad(Long staffId, String caseLoadId);

}

