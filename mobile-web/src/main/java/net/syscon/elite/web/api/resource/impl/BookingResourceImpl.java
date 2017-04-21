package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.web.api.model.*;
import net.syscon.elite.web.api.resource.BookingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class BookingResourceImpl implements BookingResource {


	private final Logger log = LoggerFactory.getLogger(getClass());

	private InmateRepository inmateRepository;
	private CaseNoteService caseNoteService;
	
	@Inject
	public void setCaseNoteService(final CaseNoteService caseNoteService) {
		this.caseNoteService = caseNoteService;
	}


	@Inject
	public void setInmateRepository(final InmateRepository inmateRepository) { this.inmateRepository = inmateRepository; }


	@Override
	public GetBookingResponse getBooking(final String query, final String orderBy, final Order order, final int offset, final int limit) throws Exception {
		final List<AssignedInmate> inmates = inmateRepository.findAllInmates(offset, limit);
		return GetBookingResponse.withJsonOK(inmates);
	}


	@Override
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
	public PostBookingByBookingIdCaseNotesResponse postBookingByBookingIdCaseNotes(String bookingId, String orderBy, Order order, int offset, int limit, CaseNote entity) throws Exception {
		return null;
	}

	@Override
	public GetBookingByBookingIdCaseNotesResponse getBookingByBookingIdCaseNotes(String bookingId, String orderBy, Order order, int offset, int limit) throws Exception {
		return null;
	}

	@Override
	public PutBookingByBookingIdCaseNotesByCaseNoteIdResponse putBookingByBookingIdCaseNotesByCaseNoteId(String bookingId, String caseNoteId, CaseNote entity) throws Exception {
		return null;
	}

	@Override
	public GetBookingByBookingIdCaseNotesByCaseNoteIdResponse getBookingByBookingIdCaseNotesByCaseNoteId(String bookingId, String caseNoteId) throws Exception {
		return null;
	}

	@Override
	public GetBookingByBookingIdAlertsResponse getBookingByBookingIdAlerts(String bookingId, String orderBy, Order order, int offset, int limit) throws Exception {
		return null;
	}

	@Override
	public GetBookingByBookingIdAlertsByAlertIdResponse getBookingByBookingIdAlertsByAlertId(String bookingId, String alertId) throws Exception {
		return null;
	}


//	@Override
//	public PostBookingByBookingIdCasenotesResponse postBookingByBookingIdCasenotes(final String bookingId, final String query, final String orderBy, final Order order, final int offset, final int limit, final CaseNote entity) throws Exception {
//		final UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//	}
//
//
//
//	@Override
//	public GetBookingByBookingIdCasenotesResponse getBookingByBookingIdCasenotes(final String bookingId, final String query, final String orderBy, final Order order, final int offset, final int limit) throws Exception {
//		final List<CaseNote> caseNotes = this.caseNoteService.getCaseNotes(bookingId, query, orderBy, order, offset, limit);
//		return GetBookingByBookingIdCasenotesResponse.withJsonOK(caseNotes);
//	}
//



}

