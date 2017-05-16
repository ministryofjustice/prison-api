package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.InmatesAlertService;
import net.syscon.elite.web.api.model.*;
import net.syscon.elite.web.api.resource.BookingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;


import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.InmatesAlertService;
import net.syscon.elite.web.api.model.Alert;
import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import net.syscon.elite.web.api.model.UserDetails;
import net.syscon.elite.web.api.resource.BookingResource;

import javax.inject.Inject;
import java.util.List;


@Component
public class BookingResourceImpl implements BookingResource {


	private final Logger log = LoggerFactory.getLogger(getClass());


	private InmateRepository inmateRepository;
	private CaseNoteService caseNoteService;
	private InmatesAlertService inmateAlertService;
	
	@Inject
	public void setCaseNoteService(final CaseNoteService caseNoteService) { this.caseNoteService = caseNoteService; }
	
	@Inject
	public void setInmateAlertService(InmatesAlertService inmateAlertService) { this.inmateAlertService = inmateAlertService; }
	
	@Inject
	public void setInmateRepository(final InmateRepository inmateRepository) { this.inmateRepository = inmateRepository; }

	
	@Override
	public GetBookingResponse getBooking(String query, String orderBy, Order order, int offset, int limit)
			throws Exception {
		final List<AssignedInmate> inmates = inmateRepository.findAllInmates(query, offset, limit, orderBy, order);
		return GetBookingResponse.withJsonOK(inmates);
	}
	
	@Override
	public GetBookingByBookingIdResponse getBookingByBookingId(String bookingId) throws Exception {
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
	public GetBookingByBookingIdCaseNotesResponse getBookingByBookingIdCaseNotes(String bookingId, String query,
			String orderBy, Order order, int offset, int limit) throws Exception {
		List<CaseNote> caseNotes = this.caseNoteService.getCaseNotes(bookingId, query, orderBy, order, offset, limit);
		return GetBookingByBookingIdCaseNotesResponse.withJsonOK(caseNotes);
	}
	
	@Override
	public PostBookingByBookingIdCaseNotesResponse postBookingByBookingIdCaseNotes(String bookingId, CaseNote entity)
			throws Exception {
		CaseNote caseNote = this.caseNoteService.createCaseNote(bookingId, "", entity);
		return PostBookingByBookingIdCaseNotesResponse.withJsonCreated(caseNote);
	}
	
	@Override
	public GetBookingByBookingIdCaseNotesByCaseNoteIdResponse getBookingByBookingIdCaseNotesByCaseNoteId(
			String bookingId, String caseNoteId) throws Exception {
		CaseNote caseNote = this.caseNoteService.getCaseNote(bookingId, caseNoteId);
		return GetBookingByBookingIdCaseNotesByCaseNoteIdResponse.withJsonOK(caseNote);
	}
	
	@Override
	public GetBookingByBookingIdAlertsResponse getBookingByBookingIdAlerts(String bookingId, String orderBy,
			Order order, String query, int offset, int limit) throws Exception {
		List<Alert> alerts = this.inmateAlertService.getInmateAlerts(bookingId, query, orderBy, order, offset, limit);
		return GetBookingByBookingIdAlertsResponse.withJsonOK(alerts);
	}
	
	@Override
	public GetBookingByBookingIdAlertsByAlertIdResponse getBookingByBookingIdAlertsByAlertId(String bookingId,
			String alertId) throws Exception {
		Alert alert = this.inmateAlertService.getInmateAlert(bookingId, alertId);
		return GetBookingByBookingIdAlertsByAlertIdResponse.withJsonOK(alert);
	}
	
	@Override
	public GetBookingByBookingIdAliasesResponse getBookingByBookingIdAliases(String bookingId, String orderBy,
			Order order, int offset, int limit) throws Exception {
		try {
			List<Alias> aliases = this.inmateRepository.findInmateAliases(Long.valueOf(bookingId), orderBy, order, offset, limit);
			return GetBookingByBookingIdAliasesResponse.withJsonOK(aliases);
		} catch (final EmptyResultDataAccessException ex) {
			final String message = String.format("Booking \"%s\" not found", bookingId);
			log.info(message);
			final HttpStatus httpStatus = new HttpStatus("404", "404", message, message, "");
			return GetBookingByBookingIdAliasesResponse.withJsonNotFound(httpStatus);
		}
	}
	
	@Override
	public PutBookingByBookingIdCaseNotesByCaseNoteIdResponse putBookingByBookingIdCaseNotesByCaseNoteId(
			String bookingId, String caseNoteId, UpdateCaseNote entity) throws Exception {
		CaseNote caseNote = this.caseNoteService.updateCaseNote(bookingId, caseNoteId, entity);
		return PutBookingByBookingIdCaseNotesByCaseNoteIdResponse.withJsonCreated(caseNote);
	}


}

