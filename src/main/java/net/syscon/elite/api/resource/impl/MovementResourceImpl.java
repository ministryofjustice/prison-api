package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.MovementResource;
import net.syscon.elite.api.support.Order;
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
    public MovementResponse getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate, String agencyId) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getRecentMovementsByDate(fromDateTime, movementDate));
    }

    @Override
    public MovementResponse getRollcount(String agencyId, boolean unassigned) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getRollCount(agencyId, unassigned));
    }

    @Override
    public MovementResponse getRollcountMovements(String agencyId, LocalDate movementDate) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getMovementCount(agencyId, movementDate));
    }

    @Override
    public MovementResponse getMovementsIn(String agencyId, LocalDate date) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getOffendersIn(agencyId, date));
    }

    @Override
    public MovementResponse getRecentMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getRecentMovementsByOffenders(offenderNumbers, movementTypes));
    }

    @Override
    public MovementResponse getEnrouteOffenderMovements(String agencyId, LocalDate movementDate, String sortFields, Order sortOrder) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getEnrouteOffenderMovements(agencyId, movementDate, sortFields, sortOrder));

    }

    @Override
    public MovementResponse getEnrouteOffenderMovementCount(String agencyId, LocalDate movementDate) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getEnrouteOffenderCount(agencyId, movementDate));
    }

    @Override
    public MovementResponse getOffendersOutToday(String agencyId, LocalDate movementsDate) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getOffendersOut(agencyId, movementsDate));
    }

    @Override
    public MovementResponse getOffendersInReception(String agencyId) {
        return MovementResponse.respond200WithApplicationJson(movementsService.getOffendersInReception(agencyId));
    }
}
