package net.syscon.elite.service;


import net.syscon.elite.v2.api.model.Alert;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;

public interface InmatesAlertService {
	List<Alert> getInmateAlerts(String bookingId, String query, String orderByField, Order order, int offset,
								int limit);
	Alert getInmateAlert(String bookingId, String alertSeqId);
}
