package net.syscon.elite.repository;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.UpdateAlert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;
import java.util.Optional;

public interface InmateAlertRepository {
	Page<Alert> getAlerts(long bookingId, String query, String orderByField, Order order, long offset, long limit);
	List<Alert> getActiveAlerts(long bookingId);
	Optional<Alert> getAlert(long bookingId, long alertSeqId);
	List<Alert> getAlertsByOffenderNos(String agencyId, List<String> offenderNos, boolean latestOnly, String query, String orderByField, Order order);

    long createNewAlert(long bookingId, CreateAlert alert, String username, String agencyId);
    Optional<Alert> updateAlert(String username, long bookingId, long alertSeq, UpdateAlert alert);
}
