package net.syscon.elite.service;


import net.syscon.elite.v2.api.model.Alert;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;

public interface InmatesAlertService {
	List<Alert> getInmateAlerts(long bookingId, String query, String orderByField, Order order, long offset, long limit);
	Alert getInmateAlert(long bookingId, long alertSeqId);
}
