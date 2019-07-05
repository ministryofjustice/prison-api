package net.syscon.elite.repository;

import net.syscon.elite.api.model.CaseLoad;

import java.util.List;
import java.util.Optional;

public interface CaseLoadRepository {
	Optional<CaseLoad> getCaseLoad(String caseLoadId);

	List<CaseLoad> getCaseLoadsByStaffId(Long staffId);

	List<CaseLoad> getCaseLoadsByUsername(String username);

	List<CaseLoad> getAllCaseLoadsByUsername(String username);

	Optional<CaseLoad> getWorkingCaseLoadByUsername(String username);
}
