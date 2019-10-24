package net.syscon.elite.repository;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.ExpireAlert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InmateAlertRepository {
    Page<Alert> getAlerts(long bookingId, String query, String orderByField, Order order, long offset, long limit);

    List<Alert> getActiveAlerts(long bookingId);

    Optional<Alert> getAlert(long bookingId, long alertSeqId);

    List<Alert> getAlertsByOffenderNos(String agencyId, List<String> offenderNos, boolean latestOnly, String query, String orderByField, Order order);

    long createNewAlert(long bookingId, CreateAlert alert);

    List<String> getAlertCandidates(LocalDateTime cutoffTimestamp);

    Optional<Alert> expireAlert(long bookingId, long alertSeq, ExpireAlert alert);
}
