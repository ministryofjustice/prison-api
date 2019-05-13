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

    public MovementResourceImpl(final MovementsService movementsService) {
        this.movementsService = movementsService;
    }

    @Override
    public List<Movement> getRecentMovementsByDate(final LocalDateTime fromDateTime, final LocalDate movementDate, final String agencyId, final List<String> movementTypes) {
        return movementsService.getRecentMovementsByDate(fromDateTime, movementDate, movementTypes);
    }

    @Override
    public List<RollCount> getRollcount(final String agencyId, final boolean unassigned) {
        return movementsService.getRollCount(agencyId, unassigned);
    }

    @Override
    public MovementCount getRollcountMovements(final String agencyId, final LocalDate movementDate) {
        return movementsService.getMovementCount(agencyId, movementDate);
    }

    @Override
    public List<OffenderIn> getMovementsIn(final String agencyId, final LocalDate date) {
        return movementsService.getOffendersIn(agencyId, date);
    }

    @Override
    public List<Movement> getRecentMovementsByOffenders(final List<String> offenderNumbers, final List<String> movementTypes) {
        return movementsService.getRecentMovementsByOffenders(offenderNumbers, movementTypes);
    }

    @Override
    public List<OffenderMovement> getEnrouteOffenderMovements(final String agencyId, final LocalDate movementDate) {
        return movementsService.getEnrouteOffenderMovements(agencyId, movementDate);
    }

    @Override
    public int getEnrouteOffenderMovementCount(final String agencyId, final LocalDate movementDate) {
        return movementsService.getEnrouteOffenderCount(agencyId, movementDate);
    }

    @Override
    public List<OffenderOutTodayDto> getOffendersOutToday(final String agencyId, final LocalDate movementsDate) {
        return movementsService.getOffendersOut(agencyId, movementsDate);
    }

    @Override
    public List<OffenderInReception> getOffendersInReception(final String agencyId) {
        return movementsService.getOffendersInReception(agencyId);
    }

    @Override
    public List<OffenderOut> getOffendersCurrentlyOut(final Long livingUnitId) {
        return movementsService.getOffendersCurrentlyOut(livingUnitId);
    }

    @Override
    public List<OffenderOut> getOffendersCurrentlyOut(final String agencyId) {
        return movementsService.getOffendersCurrentlyOut(agencyId);
    }

    @Override
    public List<Movement> getTransfers(final List<String> agencyIds, final LocalDateTime fromDateTime, final LocalDateTime toDateTime) {
        return movementsService.getTransferMovementsForAgencies(agencyIds, fromDateTime, toDateTime);
    }
}
