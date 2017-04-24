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
	public List<Alert> getInmateAlert(String bookingId, String query, String orderByField, Order order, int offset,
			int limit) {
		// TODO Auto-generated method stub
		if(null==query || "".equals(query) ) {
			query = query + "(dateExpires:gt:sysdate,or:dateExpires:is:NULL)";
		}
		//|| !query.contains("dateExpires")
		
		return inmateAlertRepository.getInmateAlert(bookingId, query, orderByField, order, offset, limit);
	}
	
}
