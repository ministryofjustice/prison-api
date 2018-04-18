package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmateAlertService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class InmateAlertServiceImpl implements InmateAlertService {

    private final InmateAlertRepository inmateAlertRepository;

    @Autowired
    public InmateAlertServiceImpl(InmateAlertRepository inmateAlertRepository) {
        this.inmateAlertRepository = inmateAlertRepository;
    }

    @Override
    @VerifyBookingAccess
    public Page<Alert> getInmateAlerts(Long bookingId, String query, String orderBy, Order order, long offset, long limit) {
        final boolean orderByBlank = StringUtils.isBlank(orderBy);

        Page<Alert> alerts = inmateAlertRepository.getInmateAlert(//
                bookingId, query, //
                orderByBlank ? "dateExpires,dateCreated" : orderBy, //
                orderByBlank ? Order.DESC : order, //
                offset, limit);

        alerts.getItems().forEach(alert -> alert.setExpired(isExpiredAlert(alert)));

        return alerts;
    }

    private boolean isExpiredAlert(Alert alert) {
        boolean expiredAlert = false;

        if (alert.getDateExpires() != null) {
            expiredAlert = alert.getDateExpires().compareTo(LocalDate.now()) <= 0;
        }

        return expiredAlert;
    }

    @Override
    @VerifyBookingAccess
    public Alert getInmateAlert(Long bookingId, Long alertSeqId) {
        final Alert alert = inmateAlertRepository.getInmateAlert(bookingId, alertSeqId)
                .orElseThrow(EntityNotFoundException.withId(alertSeqId));

        alert.setExpired(isExpiredAlert(alert));

        return alert;
    }
}
