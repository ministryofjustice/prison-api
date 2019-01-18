package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovementsRepository {

    List<Movement> getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate);

    List<RollCount> getRollCount(String agencyId, String certifiedFlag);

    MovementCount getMovementCount(String agencyId, LocalDate date);

    List<Movement> getRecentMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes);

    List<OffenderMovement> getEnrouteMovementsOffenderMovementList(String agencyId, LocalDate date);

    List<OffenderMovement> getOffendersOut(String agencyId, LocalDate movementDate);

    int getEnrouteMovementsOffenderCount(String agencyId, LocalDate date);

    List<OffenderIn> getOffendersIn(String agencyId, LocalDate movementDate);

    List<OffenderInReception> getOffendersInReception(String agencyId);

    /**
     * Retrieve offender information for those offenders currently out that normally reside within a given Living Unit (Agency internal location)
     * @param livingUnitId The 'id' of a living unit.  Living Unit ids are also internal agency location ids.
     *                     Supply the id of a landing or sub-part of a prison to obtain the set of offenders currently
     *                     out who normally reside within that location.
     * @return a List of information for each offender classed as 'out' of the given living unit.
     */
    List<OffenderOut> getOffendersCurrentlyOut(long livingUnitId);

    /**
     * Retrieve offender information for those offenders currently out that normally reside within a given Living Unit (Agency internal location)
     * @param agencyId The id of an agency (prison)
     * @return a List of information for each offender classed as 'out' of the prison.
     */
    List<OffenderOut> getOffendersCurrentlyOut(String agencyId);

}
