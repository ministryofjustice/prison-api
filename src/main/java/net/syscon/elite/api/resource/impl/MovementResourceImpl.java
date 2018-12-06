package net.syscon.elite.api.resource.impl;

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
    public GetRecentMovementsByDateResponse getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate, String agencyId) {
        return GetRecentMovementsByDateResponse.respond200WithApplicationJson(movementsService.getRecentMovementsByDate(fromDateTime, movementDate));
    }

    @Override
    public GetRollcountResponse getRollcount(String agencyId, boolean unassigned) {
        return GetRollcountResponse.respond200WithApplicationJson(movementsService.getRollCount(agencyId, unassigned));
    }

    @Override
    public GetRollcountMovementsResponse getRollcountMovements(String agencyId, LocalDate movementDate) {
        return GetRollcountMovementsResponse.respond200WithApplicationJson(movementsService.getMovementCount(agencyId, movementDate));
    }

    @Override
    public GetRecentMovementsByOffendersResponse getRecentMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes) {
        return GetRecentMovementsByOffendersResponse.respond200WithApplicationJson(movementsService.getRecentMovementsByOffenders(offenderNumbers, movementTypes));
    }

    @Override
    public GetEnrouteOffenderMovementsResponse getEnrouteOffenderMovements(String agencyId, LocalDate movementDate) {
        return GetEnrouteOffenderMovementsResponse.respond200WithApplicationJson(movementsService.getEnrouteOffenderMovements(agencyId, movementDate));

    }

    @Override
    public GetEnrouteOffenderMovementCountResponse getEnrouteOffenderMovementCount(String agencyId, LocalDate movementDate) {
        return GetEnrouteOffenderMovementCountResponse.respond200WithApplicationJson(movementsService.getEnrouteOffenderCount(agencyId, movementDate));
    }

}
