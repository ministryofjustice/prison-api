package net.syscon.elite.service;

import net.syscon.elite.api.model.MovementCount;
import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.model.RollCount;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CustodyStatusService {

    List<PrisonerCustodyStatus> getRecentMovements(LocalDateTime fromDateTime, LocalDate movementDate);

    List<RollCount> getRollCount(String agencyId, boolean unassigned);

    MovementCount getMovementCount(String agencyId, LocalDate date);
}
