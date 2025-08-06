package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.InmateAlertRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class InmateAlertService {

    private final InmateAlertRepository inmateAlertRepository;

    @Autowired
    public InmateAlertService(
            final InmateAlertRepository inmateAlertRepository
            ) {

        this.inmateAlertRepository = inmateAlertRepository;
    }

    public Page<Alert> getInmateAlerts(final Long bookingId, final String orderBy, final Order order, final long offset, final long limit) {
        final var orderByBlank = StringUtils.isBlank(orderBy);

        final var alerts = inmateAlertRepository.getAlerts(//
                bookingId, //
                orderByBlank ? "dateExpires,dateCreated" : orderBy, //
                orderByBlank ? Order.DESC : order, //
                offset, limit);

        alerts.getItems().forEach(alert -> alert.setExpired(isExpiredAlert(alert)));

        log.info("Returning {} of {} matching Alerts starting at {} for bookingId {}", alerts.getItems().size(), alerts.getTotalRecords(), alerts.getPageOffset(), bookingId);
        return alerts;
    }

    public List<Alert> getInmateAlertsByOffenderNos(final List<String> offenderNos, final boolean latestOnly, final String orderByField, final Order order) {

        final var alerts = inmateAlertRepository.getAlertsByOffenderNos(null, offenderNos, latestOnly,  orderByField, order);
        alerts.forEach(alert -> alert.setExpired(isExpiredAlert(alert)));
        log.info("Returning {} matching Alerts for Offender Numbers {}", alerts.size(), offenderNos);
        return alerts;
    }

    private boolean isExpiredAlert(final Alert alert) {
        var expiredAlert = false;

        if (alert.getDateExpires() != null) {
            expiredAlert = !alert.getDateExpires().isAfter(LocalDate.now());
        }

        return expiredAlert;
    }
}
