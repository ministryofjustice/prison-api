package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.MovementCount;
import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.model.RollCount;
import net.syscon.elite.repository.MovementsRepository;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.MovementsService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovementsServiceImpl implements MovementsService {

    private final MovementsRepository movementsRepository;

    public MovementsServiceImpl(MovementsRepository movementsRepository) {
        this.movementsRepository = movementsRepository;
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public List<PrisonerCustodyStatus> getRecentMovements(LocalDateTime fromDateTime, LocalDate movementDate) {
        return movementsRepository.getRecentMovements(fromDateTime, movementDate);
    }

    @Override
    @VerifyAgencyAccess
    public List<RollCount> getRollCount(String agencyId, boolean unassigned) {
        return movementsRepository.getRollCount(agencyId, unassigned ? "N" : "Y");
    }

    @Override
    @VerifyAgencyAccess
    public MovementCount getMovementCount(String agencyId, LocalDate date) {
        return movementsRepository.getMovementCount(agencyId, date == null ? LocalDate.now() : date);
    }
}
