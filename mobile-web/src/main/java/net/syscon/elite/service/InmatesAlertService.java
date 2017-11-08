package net.syscon.elite.service;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

public interface InmatesAlertService {
	Page<Alert> getInmateAlerts(long bookingId, String query, String orderByField, Order order, long offset, long limit);
	Alert getInmateAlert(long bookingId, long alertSeqId);
}
