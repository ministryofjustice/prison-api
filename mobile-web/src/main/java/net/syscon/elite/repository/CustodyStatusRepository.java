package net.syscon.elite.repository;

import net.syscon.elite.api.model.PrisonerCustodyStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface CustodyStatusRepository {

    List<PrisonerCustodyStatus> getRecentMovements(LocalDateTime fromDateTime);
}
