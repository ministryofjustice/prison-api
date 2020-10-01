package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.*;
import uk.gov.justice.hmpps.prison.api.resource.MovementResource;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("${api.base.path}/movements")
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
    public ResponseEntity<List<OffenderIn>> getMovementsIn(final String agencyId, final LocalDateTime fromDate, final LocalDateTime toDate, final Long pageOffset, final Long pageLimit) {
        final var page = movementsService.getOffendersIn(agencyId, fromDate, toDate, PageRequest.of(pageOffset, pageLimit));

        final var responseHeaders = new HttpHeaders();
        responseHeaders.set("Total-Records", String.valueOf(page.getTotalElements()));
        responseHeaders.set("Page-Offset", String.valueOf(page.getPageable().getOffset()));
        responseHeaders.set("Page-Limit", String.valueOf(page.getPageable().getPageSize()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(page.getContent());
    }

    @Override
    public List<Movement> getMovementsByOffenders(final List<String> offenderNumbers, final List<String> movementTypes, final Boolean latestOnly) {
        return movementsService.getMovementsByOffenders(offenderNumbers, movementTypes, latestOnly == null ? true : latestOnly);
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
    public TransferSummary getTransfers(final List<String> agencyIds,
                                        final LocalDateTime fromDateTime, final LocalDateTime toDateTime,
                                        final boolean courtEvents, final boolean releaseEvents,
                                        final boolean transferEvents, final boolean movements) {
        return movementsService.getTransferMovementsForAgencies(agencyIds, fromDateTime, toDateTime, courtEvents, releaseEvents, transferEvents, movements);
    }

    @Override
    public List<CourtEventBasic> getUpcomingCourtAppearances() {
        return movementsService.getUpcomingCourtAppearances();
    }
}
