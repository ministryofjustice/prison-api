package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.Alert;
import net.syscon.elite.web.api.resource.BookingResource.Order;

import java.util.List;
import java.util.Optional;

public interface InmateAlertRepository {
	List<Alert> getInmateAlert(String bookingId, String query, String orderByField, Order order, int offset, int limit);
	Optional<Alert> getInmateAlert(String bookingId, String alertSeqId);
}
