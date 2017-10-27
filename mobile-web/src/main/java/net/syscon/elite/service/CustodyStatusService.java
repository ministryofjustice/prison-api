package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.Order;

import java.util.List;

public interface CustodyStatusService {
    PrisonerCustodyStatus getCustodyStatus(String offenderNo);
    List<PrisonerCustodyStatus> listCustodyStatuses(String query, String orderBy, Order order);
}
