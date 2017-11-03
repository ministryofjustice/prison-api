package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmatesAlertService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class InmateAlertServiceImpl implements InmatesAlertService {

    private final InmateAlertRepository inmateAlertRepository;
    private final BookingService bookingService;

    @Autowired
    public InmateAlertServiceImpl(InmateAlertRepository inmateAlertRepository, BookingService bookingService) {
        super();
        this.inmateAlertRepository = inmateAlertRepository;
        this.bookingService = bookingService;
    }

	@Override
	public Page<Alert> getInmateAlerts(long bookingId,  String query,  String orderByField, Order order, long offset,
									   long limit) {
	    bookingService.verifyBookingAccess(bookingId);

	    String colSort = orderByField;

	    if (StringUtils.isBlank(orderByField)) {
			colSort = "dateExpires,dateCreated";
			order = Order.DESC;
		}

		Page<Alert> alerts = inmateAlertRepository.getInmateAlert(bookingId, query, colSort, order, offset, limit);

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
    public Alert getInmateAlert(long bookingId, long alertSeqId) {
        bookingService.verifyBookingAccess(bookingId);
        final Alert alert = inmateAlertRepository.getInmateAlert(bookingId, alertSeqId)
                .orElseThrow(EntityNotFoundException.withId(alertSeqId));
        alert.setExpired(isExpiredAlert(alert));
        return alert;
    }
}
