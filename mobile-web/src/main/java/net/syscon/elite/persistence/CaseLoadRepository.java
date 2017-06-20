package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.CaseLoad;

import java.util.List;

public interface CaseLoadRepository {
	CaseLoad find(String caseLoadId);
	List<CaseLoad> findCaseLoadsByUsername(String username);

}

