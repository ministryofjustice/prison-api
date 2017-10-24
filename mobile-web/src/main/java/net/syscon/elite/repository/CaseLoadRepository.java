package net.syscon.elite.repository;


import net.syscon.elite.api.model.CaseLoad;

import java.util.List;
import java.util.Optional;

public interface CaseLoadRepository {
	Optional<CaseLoad> find(String caseLoadId);
	List<CaseLoad> findCaseLoadsByUsername(String username);
	Optional<CaseLoad> getCurrentCaseLoadDetail(final String username);

}

