package net.syscon.elite.persistence;

import java.util.List;

import net.syscon.elite.web.api.model.CaseLoad;

public interface CaseLoadRepository {
	CaseLoad find(String caseLoadId);
	List<CaseLoad> findCaseLoadsByStaffId(Long staffId);

}

