package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.MovementResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CustodyStatusService;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestResource
@Path("/movements")
public class MovementResourceImpl implements MovementResource {

    private final CustodyStatusService custodyStatusService;

    public MovementResourceImpl(CustodyStatusService custodyStatusService) {
        this.custodyStatusService = custodyStatusService;
    }

    @Override
    public GetRecentMovementsResponse getRecentMovements(LocalDateTime fromDateTime, LocalDate movementDate, String agencyId) {
        return GetRecentMovementsResponse
                .respond200WithApplicationJson(custodyStatusService.getRecentMovements(fromDateTime, movementDate));
    }

    @Override
    public GetRollcountResponse getRollcount(String agencyId, boolean unassigned) {
        return GetRollcountResponse.respond200WithApplicationJson(custodyStatusService.getRollCount(agencyId, unassigned));
    }

    @Override
    public GetRollcountMovementsResponse getRollcountMovements(String agencyId, LocalDate movementDate) {
        return GetRollcountMovementsResponse.respond200WithApplicationJson(custodyStatusService.getMovementCount(agencyId, movementDate));
    }
}
