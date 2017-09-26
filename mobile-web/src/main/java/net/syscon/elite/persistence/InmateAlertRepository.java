package net.syscon.elite.persistence;


import net.syscon.elite.v2.api.model.Alert;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;
import java.util.Optional;

public interface InmateAlertRepository {
	List<Alert> getInmateAlert(long bookingId, String query, String orderByField, Order order, long offset, long limit);
	Optional<Alert> getInmateAlert(long bookingId, long alertSeqId);
}
