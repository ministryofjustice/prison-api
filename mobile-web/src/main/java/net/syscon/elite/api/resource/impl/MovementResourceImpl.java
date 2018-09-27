package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.MovementResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.MovementsService;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestResource
@Path("/movements")
public class MovementResourceImpl implements MovementResource {

    private final MovementsService movementsService;

    public MovementResourceImpl(MovementsService movementsService) {
        this.movementsService = movementsService;
    }

    @Override
    public GetRecentMovementsResponse getRecentMovements(LocalDateTime fromDateTime, LocalDate movementDate, String agencyId) {
        return GetRecentMovementsResponse
                .respond200WithApplicationJson(movementsService.getRecentMovements(fromDateTime, movementDate));
    }

    @Override
    public GetRollcountResponse getRollcount(String agencyId, boolean unassigned) {
        return GetRollcountResponse.respond200WithApplicationJson(movementsService.getRollCount(agencyId, unassigned));
    }

    @Override
    public GetRollcountMovementsResponse getRollcountMovements(String agencyId, LocalDate movementDate) {
        return GetRollcountMovementsResponse.respond200WithApplicationJson(movementsService.getMovementCount(agencyId, movementDate));
    }
}
