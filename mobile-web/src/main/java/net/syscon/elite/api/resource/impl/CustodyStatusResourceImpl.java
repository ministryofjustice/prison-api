package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.CustodyStatusResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.MovementsService;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestResource
@Path("/custody-statuses")
public class CustodyStatusResourceImpl implements CustodyStatusResource {

    private final MovementsService movementsService;

    public CustodyStatusResourceImpl(MovementsService movementsService) {
        this.movementsService = movementsService;
    }

    @Override
    public GetRecentMovementsResponse getRecentMovements(LocalDateTime fromDateTime, LocalDate movementDate) {
        return GetRecentMovementsResponse
                .respond200WithApplicationJson(movementsService.getRecentMovementsByDate(fromDateTime, movementDate));
    }
}
