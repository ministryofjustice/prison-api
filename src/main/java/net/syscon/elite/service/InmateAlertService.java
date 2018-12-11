package net.syscon.elite.service;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;

public interface InmateAlertService {
	Page<Alert> getInmateAlerts(Long bookingId, String query, String orderBy, Order order, long offset, long limit);

	Alert getInmateAlert(Long bookingId, Long alertSeqId);

	List<Alert> getInmateAlertsByOffenderNosAtAgency(String agencyId, List<String>offenderNos);
	List<Alert> getInmateAlertsByOffenderNos(List<String>offenderNos);
}
