package net.syscon.elite.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.persistence.InmateAlertRepository;
import net.syscon.elite.service.InmatesAlertService;
import net.syscon.elite.web.api.model.Alert;
import net.syscon.elite.web.api.resource.BookingResource.Order;

@Service
@Transactional
public class InmateAlertServiceImpl implements InmatesAlertService {
	
	private InmateAlertRepository inmateAlertRepository;
	@Inject
	public void setInmateAlertRepository(InmateAlertRepository inmateAlertRepository) {
		this.inmateAlertRepository = inmateAlertRepository;
	}


	@Override
	public List<Alert> getInmateAlerts(String bookingId, String query, String orderByField, Order order, int offset,
			int limit) {
		if(null==query || "".equals(query) ) {
			query = "(dateExpires:gt:sysdate,or:dateExpires:is:NULL)";
		}
		return inmateAlertRepository.getInmateAlert(bookingId, query, orderByField, order, offset, limit);
	}


	@Override
	public Alert getInmateAlert(String bookingId, String alertSeqId) {
		// TODO Auto-generated method stub
		return inmateAlertRepository.getInmateAlert(bookingId, alertSeqId);
	}
	
}
