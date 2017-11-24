package net.syscon.elite.service;

import net.syscon.elite.api.model.CaseLoad;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CaseLoadService {

    Optional<CaseLoad> getCaseLoad(String caseLoadId);

    List<CaseLoad> getCaseLoadsForUser(String username);

    Optional<CaseLoad> getWorkingCaseLoadForUser(String username);

    Set<String> getCaseLoadIdsForUser(String username);
}
