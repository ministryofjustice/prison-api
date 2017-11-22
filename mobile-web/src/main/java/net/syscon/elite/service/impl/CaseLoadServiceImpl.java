package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.repository.CaseLoadRepository;
import net.syscon.elite.service.CaseLoadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class CaseLoadServiceImpl implements CaseLoadService {
    private final CaseLoadRepository caseLoadRepository;

    public CaseLoadServiceImpl(CaseLoadRepository caseLoadRepository) {
        this.caseLoadRepository = caseLoadRepository;
    }

    @Override
    public List<CaseLoad> findAllCaseLoadsForUser(String username) {
        return null;
    }

    @Override
    public Optional<CaseLoad> getWorkingCaseLoadForUser(String username) {
        return caseLoadRepository.getCurrentCaseLoadDetail(username);
    }
}
