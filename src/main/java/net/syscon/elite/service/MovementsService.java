package net.syscon.elite.service;

import net.syscon.elite.api.model.Movement;
import net.syscon.elite.api.model.MovementCount;
import net.syscon.elite.api.model.RollCount;
import net.syscon.elite.api.model.OffenderMovement;
import net.syscon.elite.api.model.OffenderOutTodayDto;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovementsService {

    List<Movement> getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate);

    List<Movement> getRecentMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes);

    List<RollCount> getRollCount(String agencyId, boolean unassigned);

    MovementCount getMovementCount(String agencyId, LocalDate date);

    List<OffenderMovement> getEnrouteOffenderMovements(String agencyId, LocalDate date);

    int getEnrouteOffenderCount(String agencyId, LocalDate date);

    List<OffenderOutTodayDto> getOffendersOutToday();
}
