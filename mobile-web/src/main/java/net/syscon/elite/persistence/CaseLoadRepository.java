package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.CaseLoad;

import java.util.List;
import java.util.Optional;

public interface CaseLoadRepository {
	Optional<CaseLoad> find(String caseLoadId);
	List<CaseLoad> findCaseLoadsByUsername(String username);

}

