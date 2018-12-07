package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Movement;
import net.syscon.elite.api.model.MovementCount;
import net.syscon.elite.api.model.OffenderMovement;
import net.syscon.elite.api.model.RollCount;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.MovementsRepository;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.MovementsService;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
    public List<Movement> getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate) {
        return movementsRepository.getRecentMovementsByDate(fromDateTime, movementDate);
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public List<Movement> getRecentMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes) {
        final List<Movement> movements = movementsRepository.getRecentMovementsByOffenders(offenderNumbers, movementTypes);
        movements.forEach(m -> {
            m.setFromAgencyDescription(LocationProcessor.formatLocation(m.getFromAgencyDescription()));
            m.setToAgencyDescription(LocationProcessor.formatLocation(m.getToAgencyDescription()));
        });
        return movements;
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

    @Override
    @VerifyAgencyAccess
    public List<OffenderMovement> getEnrouteOffenderMovements(String agencyId, LocalDate date, String orderByFields, Order order) {
        String sortFields = StringUtils.defaultString(orderByFields, "lastName,firstName");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);
        final LocalDate defaultedDate = date == null ? LocalDate.now() : date;
        final List<OffenderMovement> movements = movementsRepository.getEnrouteMovementsOffenderMovementList(agencyId, defaultedDate, sortFields, sortOrder);
        movements.forEach(m -> {
            m.setFromAgencyDescription(LocationProcessor.formatLocation(m.getFromAgencyDescription()));
            m.setToAgencyDescription(LocationProcessor.formatLocation(m.getToAgencyDescription()));
        });
        return movements;
    }

    @Override
    public int getEnrouteOffenderCount(String agencyId, LocalDate date) {
        final LocalDate defaultedDate = date == null ? LocalDate.now() : date;
        return movementsRepository.getEnrouteMovementsOffenderCount(agencyId, defaultedDate);
    }
}
