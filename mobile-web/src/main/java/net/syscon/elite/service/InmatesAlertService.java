package net.syscon.elite.service;

import net.syscon.elite.web.api.model.Alert;
import net.syscon.elite.web.api.resource.BookingResource.Order;

import java.util.List;

public interface InmatesAlertService {
	List<Alert> getInmateAlerts(String bookingId, String query, String orderByField, Order order, int offset,
			int limit);
	Alert getInmateAlert(String bookingId, String alertSeqId);
}
