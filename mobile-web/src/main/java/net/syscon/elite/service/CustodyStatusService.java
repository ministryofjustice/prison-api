package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;

import java.time.LocalDate;
import java.util.List;

public interface CustodyStatusService {
    PrisonerCustodyStatus getCustodyStatus(String offenderNo, LocalDate onDate);
    List<PrisonerCustodyStatus> listCustodyStatuses(List<CustodyStatusCode> custodyStatusCodes, LocalDate onDate, Order order);
}
