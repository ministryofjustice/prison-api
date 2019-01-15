package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.MovementResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.MovementsService;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestResource
@Path("/movements")
public class MovementResourceImpl implements MovementResource {

    private final MovementsService movementsService;

    public MovementResourceImpl(MovementsService movementsService) {
        this.movementsService = movementsService;
    }

    @Override
    public List<Movement> getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate, String agencyId) {
        return movementsService.getRecentMovementsByDate(fromDateTime, movementDate);
    }

    @Override
    public List<RollCount> getRollcount(String agencyId, boolean unassigned) {
        return movementsService.getRollCount(agencyId, unassigned);
    }

    @Override
    public MovementCount getRollcountMovements(String agencyId, LocalDate movementDate) {
        return movementsService.getMovementCount(agencyId, movementDate);
    }

    @Override
    public List<OffenderIn> getMovementsIn(String agencyId, LocalDate date) {
        return movementsService.getOffendersIn(agencyId, date);
    }

    @Override
    public List<Movement> getRecentMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes) {
        return movementsService.getRecentMovementsByOffenders(offenderNumbers, movementTypes);
    }

    @Override
    public List<OffenderMovement> getEnrouteOffenderMovements(String agencyId, LocalDate movementDate) {
        return movementsService.getEnrouteOffenderMovements(agencyId, movementDate);
    }

    @Override
    public int getEnrouteOffenderMovementCount(String agencyId, LocalDate movementDate) {
        return movementsService.getEnrouteOffenderCount(agencyId, movementDate);
    }

    @Override
    public List<OffenderOutTodayDto> getOffendersOutToday(String agencyId, LocalDate movementsDate) {
        return movementsService.getOffendersOut(agencyId, movementsDate);
    }

    @Override
    public List<OffenderInReception> getOffendersInReception(String agencyId) {
        return movementsService.getOffendersInReception(agencyId);
    }
}
