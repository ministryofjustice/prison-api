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
}
