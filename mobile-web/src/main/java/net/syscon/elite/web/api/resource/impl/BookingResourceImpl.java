package net.syscon.elite.web.api.resource.impl;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.resource.BookingResource;

@Component
public class BookingResourceImpl implements BookingResource {


	private final Logger log = LoggerFactory.getLogger(getClass());

	private InmateRepository inmateRepository;


	@Inject
	public void setInmateRepository(final InmateRepository inmateRepository) { this.inmateRepository = inmateRepository; }


	@Override
	public GetBookingResponse getBooking(final String orderBy, final Order order, final int offset, final int limit) throws Exception {
		final List<AssignedInmate> inmates = inmateRepository.findAllInmates(offset, limit);
		return GetBookingResponse.withJsonOK(inmates);
	}

	@Override
	@SuppressWarnings("squid:S1166")
	public GetBookingByBookingIdResponse getBookingByBookingId(final String bookingId) throws Exception {
		try {
			final InmateDetails inmate = inmateRepository.findInmate(Long.valueOf(bookingId));
			return GetBookingByBookingIdResponse.withJsonOK(inmate);
		} catch (final EmptyResultDataAccessException ex) {
			final String message = String.format("Booking \"%s\" not found", bookingId);
			log.info(message);
			final HttpStatus httpStatus = new HttpStatus("404", "404", message, message, "");
			return GetBookingByBookingIdResponse.withJsonNotFound(httpStatus);
		}
	}

	@Override
	public GetBookingByBookingIdMovementsResponse getBookingByBookingIdMovements(final String bookingId, final String orderBy, final Order order,final int offset, final int limit) throws Exception {
		return null;
	}

}

