package net.syscon.elite.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.persistence.CaseLoadRepository;
import net.syscon.elite.persistence.FinanceRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.FinanceService;

@Service
@Transactional(readOnly = true)
public class FinanceServiceImpl implements FinanceService {

    private final FinanceRepository financeRepository;
    private final CaseLoadRepository caseLoadRepository;

    @Autowired
    public FinanceServiceImpl(FinanceRepository financeRepository, CaseLoadRepository caseLoadRepository) {
        this.financeRepository = financeRepository;
        this.caseLoadRepository = caseLoadRepository;
    }

    @Override
    public Account getAccount(final long bookingId) {
        return financeRepository.getAccount(bookingId, getUserCaseloadIds())
                .orElseThrow(new EntityNotFoundException(String.valueOf(bookingId)));
    }

    protected Set<String> getUserCaseloadIds() {
        return caseLoadRepository.findCaseLoadsByUsername(UserSecurityUtils.getCurrentUsername()).stream()
                .map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
    }
}
