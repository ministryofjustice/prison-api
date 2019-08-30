package net.syscon.elite.repository;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.UpdateAlert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;
import java.util.Optional;

public interface InmateAlertRepository {
	Page<Alert> getInmateAlerts(long bookingId, String query, String orderByField, Order order, long offset, long limit);
	Optional<Alert> getInmateAlerts(long bookingId, long alertSeqId);
	List<Alert> getInmateAlertsByOffenderNos(String agencyId, List<String> offenderNos, boolean latestOnly, String query, String orderByField, Order order);

    long createNewAlert(final long bookingId, final CreateAlert alert, final String username, String agencyId);
    Optional<Alert> updateAlert(final String username, long bookingId, long alertSeq, final UpdateAlert alert);
}
