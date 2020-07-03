package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.CourtEvent;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementCount;
import uk.gov.justice.hmpps.prison.api.model.MovementSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.api.model.ReleaseEvent;
import uk.gov.justice.hmpps.prison.api.model.RollCount;
import uk.gov.justice.hmpps.prison.api.model.TransferEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovementsRepository {

    List<Movement> getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate, List<String> movementTypes);

    List<RollCount> getRollCount(String agencyId, String certifiedFlag);

    MovementCount getMovementCount(String agencyId, LocalDate date);

    Movement getMovementByBookingIdAndSequence(final long bookingId, final int sequenceNumber);

    List<Movement> getMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes, final boolean latestOnly);

    List<OffenderMovement> getEnrouteMovementsOffenderMovementList(String agencyId, LocalDate date);

    List<OffenderMovement> getOffendersOut(String agencyId, LocalDate movementDate);

    int getEnrouteMovementsOffenderCount(String agencyId, LocalDate date);

    List<OffenderIn> getOffendersIn(String agencyId, LocalDate movementDate);

    List<OffenderInReception> getOffendersInReception(String agencyId);

    /**
     * Retrieve offender information for those offenders currently out that normally reside within a given Living Unit (Agency internal location)
     *
     * @param livingUnitId The 'id' of a living unit.  Living Unit ids are also internal agency location ids.
     *                     Supply the id of a landing or sub-part of a prison to obtain the set of offenders currently
     *                     out who normally reside within that location.
     * @return a List of information for each offender classed as 'out' of the given living unit.
     */
    List<OffenderOut> getOffendersCurrentlyOut(long livingUnitId);

    /**
     * Retrieve offender information for those offenders currently out that normally reside within a given Living Unit (Agency internal location)
     *
     * @param agencyId The id of an agency (prison)
     * @return a List of information for each offender classed as 'out' of the prison.
     */
    List<OffenderOut> getOffendersCurrentlyOut(String agencyId);

    List<MovementSummary> getCompletedMovementsForAgencies(List<String> agencies, LocalDateTime from, LocalDateTime to);

    List<CourtEvent> getCourtEvents(List<String> agencies, LocalDateTime from, LocalDateTime to);

    List<TransferEvent> getOffenderTransfers(List<String> agencies, LocalDateTime from, LocalDateTime to);

    List<ReleaseEvent> getOffenderReleases(List<String> agencies, LocalDateTime from, LocalDateTime to);
}
