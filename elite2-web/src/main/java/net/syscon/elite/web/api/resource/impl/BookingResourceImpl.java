package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateDetail;
import net.syscon.elite.web.api.resource.BookingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class BookingResourceImpl implements BookingResource {


	private final Logger log = LoggerFactory.getLogger(getClass());

	private InmateRepository inmateRepository;


	@Inject
	public void setInmateRepository(final InmateRepository inmateRepository) { this.inmateRepository = inmateRepository; }


	@Override
	public GetBookingResponse getBooking(String orderBy, Order order, int offset, int limit) throws Exception {
		final List<AssignedInmate> inmates = inmateRepository.findAllInmates(offset, limit);
		return GetBookingResponse.withJsonOK(inmates);
	}

	@Override
	public GetBookingByBookingIdResponse getBookingByBookingId(String bookingId) throws Exception {
		InmateDetail inmate = inmateRepository.findInmate(Long.valueOf(bookingId));
		return GetBookingByBookingIdResponse.withJsonOK(inmate);
	}

	@Override
	public GetBookingByBookingIdMovementsResponse getBookingByBookingIdMovements(String bookingId, String orderBy, Order order,int offset, int limit) throws Exception {
		return null;
	}

}

