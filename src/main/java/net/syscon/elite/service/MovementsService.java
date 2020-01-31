package net.syscon.elite.service;

import net.syscon.elite.api.model.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovementsService {

    List<Movement> getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate, List<String> movementTypes);

    Movement getMovementByBookingIdAndSequence(@NotNull final Long bookingId, @NotNull final Integer sequenceNumber);

    List<Movement> getMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes, boolean latestOnly);

    List<RollCount> getRollCount(String agencyId, boolean unassigned);

    MovementCount getMovementCount(String agencyId, LocalDate date);

    List<OffenderMovement> getEnrouteOffenderMovements(String agencyId, LocalDate date);

    int getEnrouteOffenderCount(String agencyId, LocalDate date);

    List<OffenderOutTodayDto> getOffendersOut(String agencyId, LocalDate movementDate);

    List<OffenderIn> getOffendersIn(String agencyId, LocalDate date);

    List<OffenderInReception> getOffendersInReception(String agencyId);

    List<OffenderOut> getOffendersCurrentlyOut(long livingUnitId);

    List<OffenderOut> getOffendersCurrentlyOut(String agencyId);

    TransferSummary getTransferMovementsForAgencies(List<String> agencyIds, LocalDateTime fromDateTime, LocalDateTime toDateTime,
                                                    boolean courtEvents, boolean releaseEvents, boolean transferEvents, boolean movements);
}
