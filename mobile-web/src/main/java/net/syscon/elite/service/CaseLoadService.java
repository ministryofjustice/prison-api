package net.syscon.elite.service;

import net.syscon.elite.api.model.CaseLoad;

import java.util.List;
import java.util.Optional;

public interface CaseLoadService {
    List<CaseLoad> findAllCaseLoadsForUser(String username);

    Optional<CaseLoad> getWorkingCaseLoadForUser(String username);
}
