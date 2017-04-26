package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.web.api.model.Alert;
import net.syscon.elite.web.api.resource.BookingResource.Order;

public interface InmatesAlertService {
	public List<Alert> getInmateAlerts(String bookingId, String query, String orderByField, Order order, int offset,
			int limit);
	public Alert getInmateAlert(String bookingId, String alertSeqId);
}
