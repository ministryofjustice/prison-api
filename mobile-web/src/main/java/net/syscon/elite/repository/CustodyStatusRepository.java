package net.syscon.elite.repository;

import net.syscon.elite.service.support.CustodyStatusDto;

import java.util.List;
import java.util.Optional;

public interface CustodyStatusRepository {
    List<CustodyStatusDto> listCustodyStatuses();
    Optional<CustodyStatusDto> getCustodyStatus(String offenderNo);
}
