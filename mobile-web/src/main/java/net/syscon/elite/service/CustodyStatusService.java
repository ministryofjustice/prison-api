package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonerCustodyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CustodyStatusService {

    List<PrisonerCustodyStatus> getRecentMovements(LocalDateTime fromDateTime, LocalDate movementDate);
}
