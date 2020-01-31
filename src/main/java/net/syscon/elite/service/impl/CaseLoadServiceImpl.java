package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.repository.CaseLoadRepository;
import net.syscon.elite.service.CaseLoadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CaseLoadServiceImpl implements CaseLoadService {
    private final CaseLoadRepository caseLoadRepository;

    public CaseLoadServiceImpl(final CaseLoadRepository caseLoadRepository) {
        this.caseLoadRepository = caseLoadRepository;
    }

    @Override
    public Optional<CaseLoad> getCaseLoad(final String caseLoadId) {
        return caseLoadRepository.getCaseLoad(caseLoadId);
    }

    @Override
    public List<CaseLoad> getCaseLoadsForUser(final String username, final boolean allCaseloads) {
        return allCaseloads ? caseLoadRepository.getAllCaseLoadsByUsername(username) : caseLoadRepository.getCaseLoadsByUsername(username);
    }

    @Override
    public Optional<CaseLoad> getWorkingCaseLoadForUser(final String username) {
        return caseLoadRepository.getWorkingCaseLoadByUsername(username);
    }

    @Override
    public Set<String> getCaseLoadIdsForUser(final String username, final boolean allCaseloads) {
        return getCaseLoadsForUser(username, allCaseloads).stream().map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
    }
}
