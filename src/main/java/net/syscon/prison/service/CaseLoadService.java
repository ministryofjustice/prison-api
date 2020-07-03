package net.syscon.prison.service;

import net.syscon.prison.api.model.CaseLoad;
import net.syscon.prison.repository.CaseLoadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CaseLoadService {
    private final CaseLoadRepository caseLoadRepository;

    public CaseLoadService(final CaseLoadRepository caseLoadRepository) {
        this.caseLoadRepository = caseLoadRepository;
    }

    public Optional<CaseLoad> getCaseLoad(final String caseLoadId) {
        return caseLoadRepository.getCaseLoad(caseLoadId);
    }

    public List<CaseLoad> getCaseLoadsForUser(final String username, final boolean allCaseloads) {
        return allCaseloads ? caseLoadRepository.getAllCaseLoadsByUsername(username) : caseLoadRepository.getCaseLoadsByUsername(username);
    }

    public Optional<CaseLoad> getWorkingCaseLoadForUser(final String username) {
        return caseLoadRepository.getWorkingCaseLoadByUsername(username);
    }

    public Set<String> getCaseLoadIdsForUser(final String username, final boolean allCaseloads) {
        return getCaseLoadsForUser(username, allCaseloads).stream().map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
    }
}
