package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.service.CustodyStatusService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustodyStatusServiceImpl implements CustodyStatusService {

    private final CustodyStatusRepository custodyStatusRepository;

    public CustodyStatusServiceImpl(CustodyStatusRepository custodyStatusRepository) {
        this.custodyStatusRepository = custodyStatusRepository;
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public List<PrisonerCustodyStatus> getRecentMovements(LocalDateTime fromDateTime) {
        return custodyStatusRepository.getRecentMovements(fromDateTime);
    }
}
