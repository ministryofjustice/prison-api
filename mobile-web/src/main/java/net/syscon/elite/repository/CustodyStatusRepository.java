package net.syscon.elite.repository;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.model.RollCount;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CustodyStatusRepository {

    List<PrisonerCustodyStatus> getRecentMovements(LocalDateTime fromDateTime, LocalDate movementDate);

    List<RollCount> getRollCount(String agencyId);
}
