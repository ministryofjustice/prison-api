package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InmateAlertRepository {
    Page<Alert> getAlerts(long bookingId, String query, String orderByField, Order order, long offset, long limit);

    List<Alert> getActiveAlerts(long bookingId);

    Optional<Alert> getAlert(long bookingId, long alertSeqId);

    List<Alert> getAlertsByOffenderNos(String agencyId, List<String> offenderNos, boolean latestOnly, String query, String orderByField, Order order);

    long createNewAlert(long bookingId, CreateAlert alert);

    Page<String> getAlertCandidates(LocalDateTime cutoffTimestamp, final long offset, final long limit);

    Optional<Alert> updateAlert(long bookingId, long alertSeq, AlertChanges alert);
}
