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

import static java.lang.String.format;

@Service
@Transactional(readOnly = true)
public class CaseLoadServiceImpl implements CaseLoadService {
    private final CaseLoadRepository caseLoadRepository;

    public CaseLoadServiceImpl(CaseLoadRepository caseLoadRepository) {
        this.caseLoadRepository = caseLoadRepository;
    }

    @Override
    public Optional<CaseLoad> getCaseLoad(String caseLoadId) {
        return caseLoadRepository.getCaseLoad(caseLoadId);
    }

    @Override
    public List<CaseLoad> getCaseLoadsForUser(String username, boolean allCaseloads) {
        String query = null;
        if (!allCaseloads) {
            query = format("type:eq:'%s',and:caseloadFunction:neq:'%s'", "INST", "ADMIN");
        }
        return caseLoadRepository.getCaseLoadsByUsername(username, query);
    }

    @Override
    public Optional<CaseLoad> getWorkingCaseLoadForUser(String username) {
        return caseLoadRepository.getWorkingCaseLoadByUsername(username);
    }

    @Override
    public Set<String> getCaseLoadIdsForUser(String username, boolean allCaseloads) {
        return getCaseLoadsForUser(username, allCaseloads).stream().map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
    }

}
