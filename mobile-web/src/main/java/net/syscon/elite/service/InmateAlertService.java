package net.syscon.elite.service;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

public interface InmateAlertService {
	Page<Alert> getInmateAlerts(Long bookingId, String query, String orderBy, Order order, long offset, long limit);

	Alert getInmateAlert(Long bookingId, Long alertSeqId);
}
