package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.InmateAlertRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmatesAlertService;
import net.syscon.elite.web.api.model.Alert;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InmateAlertServiceImpl implements InmatesAlertService {
	@Autowired
	private InmateAlertRepository inmateAlertRepository;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	public List<Alert> getInmateAlerts(String bookingId, final String query, final String orderByField, Order order, int offset,
			int limit) {
	    String colSort = orderByField;
		if (StringUtils.isBlank(orderByField)) {
			colSort = "dateExpires,dateCreated";
			order = Order.desc;
		}
		final List<Alert> alerts = inmateAlertRepository.getInmateAlert(bookingId, query, colSort, order, offset, limit);
		alerts.forEach(alert -> alert.setExpired(isExpiredAlert(alert)));
		return alerts;
	}

	private boolean isExpiredAlert(Alert alert) {
		boolean expiredAlert = false;
		if (alert.getDateExpires() != null) {
            LocalDate expiryDate = LocalDate.parse(alert.getDateExpires(), DATE_FORMAT);
            expiredAlert = expiryDate.compareTo(LocalDate.now()) <= 0;
        }
		return expiredAlert;
	}

	@Override
	public Alert getInmateAlert(String bookingId, String alertSeqId) {
        final Alert alert = inmateAlertRepository.getInmateAlert(bookingId, alertSeqId).orElseThrow(new EntityNotFoundException(String.valueOf(alertSeqId)));
        alert.setExpired(isExpiredAlert(alert));
        return alert;
	}
	
}
