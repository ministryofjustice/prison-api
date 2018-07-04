package net.syscon.elite.repository;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.Optional;

public interface InmateAlertRepository {
	Page<Alert> getInmateAlert(long bookingId, String query, String orderByField, Order order, long offset, long limit);
	Optional<Alert> getInmateAlert(long bookingId, long alertSeqId);
}
