package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.InmateAlertRepository;
import net.syscon.elite.service.InmatesAlertService;
import net.syscon.elite.web.api.model.Alert;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InmateAlertServiceImpl implements InmatesAlertService {
	
	private final InmateAlertRepository inmateAlertRepository;

	@Inject
	public InmateAlertServiceImpl(InmateAlertRepository inmateAlertRepository) {
		this.inmateAlertRepository = inmateAlertRepository;
	}

	@Override
	public List<Alert> getInmateAlerts(String bookingId, String query, String orderByField, Order order, int offset,
			int limit) {
		if( null == orderByField || "".equals(orderByField) ) {
			orderByField = "dateExpires";
			order = Order.desc;
		}
		return inmateAlertRepository.getInmateAlert(bookingId, query, orderByField, order, offset, limit);
	}

	@Override
	public Alert getInmateAlert(String bookingId, String alertSeqId) {
		return inmateAlertRepository.getInmateAlert(bookingId, alertSeqId);
	}
	
}
