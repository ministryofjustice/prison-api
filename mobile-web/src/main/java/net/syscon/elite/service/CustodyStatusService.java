package net.syscon.elite.service;

import net.syscon.elite.api.model.CustodyStatus;
import net.syscon.elite.api.support.Order;

import java.util.List;

public interface CustodyStatusService {
    CustodyStatus getCustodyStatus(String offenderNo);
    List<CustodyStatus> listCustodyStatuses(String query, String orderBy, Order order);
}
