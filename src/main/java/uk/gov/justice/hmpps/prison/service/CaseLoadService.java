package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.repository.CaseLoadRepository;

import java.util.Collections;
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

    public List<CaseLoad> getCaseLoadsForUser(final String username, final boolean allCaseloads) {
        if (StringUtils.isBlank(username)) return Collections.emptyList();
        return allCaseloads ? caseLoadRepository.getAllCaseLoadsByUsername(username) : caseLoadRepository.getCaseLoadsByUsername(username);
    }

    public Optional<CaseLoad> getWorkingCaseLoadForUser(final String username) {
        if (StringUtils.isBlank(username)) return Optional.empty();
        return caseLoadRepository.getWorkingCaseLoadByUsername(username);
    }

    public Set<String> getCaseLoadIdsForUser(final String username, final boolean allCaseloads) {
        return getCaseLoadsForUser(username, allCaseloads).stream().map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
    }
}
