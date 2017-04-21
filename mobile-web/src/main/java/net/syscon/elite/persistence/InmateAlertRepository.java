package net.syscon.elite.persistence;

import java.util.List;

import net.syscon.elite.web.api.model.Alert;
import net.syscon.elite.web.api.resource.BookingResource.Order;

public interface InmateAlertRepository {
	List<Alert> getInmateAlert(String bookingId, String query, String orderByField, Order order, int offset, int limit);
}
