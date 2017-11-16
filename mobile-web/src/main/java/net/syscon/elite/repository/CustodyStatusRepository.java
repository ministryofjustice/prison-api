package net.syscon.elite.repository;

import net.syscon.elite.service.support.CustodyStatusDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustodyStatusRepository {
    List<CustodyStatusDto> listCustodyStatuses();
    List<CustodyStatusDto> listCustodyStatuses(LocalDate onDate);
    Optional<CustodyStatusDto> getCustodyStatus(String offenderNo);
    Optional<CustodyStatusDto> getCustodyStatus(String offenderNo, LocalDate onDate);
}
