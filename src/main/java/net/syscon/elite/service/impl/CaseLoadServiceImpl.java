package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.repository.CaseLoadRepository;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.support.LocationProcessor;
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
            query = format("type:eq:'%s'", "INST");
        }
        List<CaseLoad> caseLoadsByUsername = caseLoadRepository.getCaseLoadsByUsername(username, query);
        caseLoadsByUsername.forEach(cl -> cl.setDescription(LocationProcessor.formatLocation(cl.getDescription())));
        return caseLoadsByUsername;
    }

    @Override
    public Optional<CaseLoad> getWorkingCaseLoadForUser(String username) {
        Optional<CaseLoad> workingCaseLoadByUsername = caseLoadRepository.getWorkingCaseLoadByUsername(username);
        if (workingCaseLoadByUsername.isPresent()) {
            CaseLoad caseLoad = workingCaseLoadByUsername.get();
            caseLoad.setDescription(LocationProcessor.formatLocation(caseLoad.getDescription()));
        }
        return workingCaseLoadByUsername;
    }

    @Override
    public Set<String> getCaseLoadIdsForUser(String username, boolean allCaseloads) {
        return getCaseLoadsForUser(username, allCaseloads).stream().map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
    }

}
