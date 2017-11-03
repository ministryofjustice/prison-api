package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;

import java.util.List;

public interface CustodyStatusService {
    PrisonerCustodyStatus getCustodyStatus(String offenderNo);
    List<PrisonerCustodyStatus> listCustodyStatuses(Order order);
    List<PrisonerCustodyStatus> listCustodyStatuses(CustodyStatusCode custodyStatusCode);
    List<PrisonerCustodyStatus> listCustodyStatuses(List<CustodyStatusCode> custodyStatusCodes, Order order);
}
