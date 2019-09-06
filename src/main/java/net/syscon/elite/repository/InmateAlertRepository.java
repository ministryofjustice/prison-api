package net.syscon.elite.repository;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.UpdateAlert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;
import java.util.Optional;

public interface InmateAlertRepository {
	Page<Alert> getAlerts(final long bookingId, final String query, final String orderByField, final Order order, final long offset, final long limit);
	List<Alert> getActiveAlerts(final long bookingId);
	Optional<Alert> getAlert(final long bookingId, final long alertSeqId);
	List<Alert> getAlertsByOffenderNos(final String agencyId, final List<String> offenderNos, final boolean latestOnly, final String query, final String orderByField, final Order order);

    long createNewAlert(final long bookingId, final CreateAlert alert, final String username, String agencyId);
    Optional<Alert> updateAlert(final String username, long bookingId, long alertSeq, final UpdateAlert alert);
}
