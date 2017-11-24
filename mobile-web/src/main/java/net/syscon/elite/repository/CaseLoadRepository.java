package net.syscon.elite.repository;

import net.syscon.elite.api.model.CaseLoad;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CaseLoadRepository {
	Optional<CaseLoad> getCaseLoad(String caseLoadId);

	List<CaseLoad> getCaseLoadsByUsername(String username);

	Optional<CaseLoad> getWorkingCaseLoadByUsername(String username);

	Set<String> getCaseLoadIdsByUsername(String username);
}
