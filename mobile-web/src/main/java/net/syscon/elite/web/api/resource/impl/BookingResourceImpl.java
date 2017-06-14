package net.syscon.elite.web.api.resource.impl;

import java.util.List;

import javax.inject.Inject;

import net.syscon.elite.web.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.InmatesAlertService;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.util.MetaDataFactory;

@Component
public class BookingResourceImpl implements BookingResource {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final InmateRepository inmateRepository;
	private final CaseNoteService caseNoteService;
	private final InmatesAlertService inmateAlertService;

	@Inject
	public BookingResourceImpl(InmateRepository inmateRepository, CaseNoteService caseNoteService, InmatesAlertService inmateAlertService) {
		this.inmateRepository = inmateRepository;
		this.caseNoteService = caseNoteService;
		this.inmateAlertService = inmateAlertService;
	}

	@Override
	public GetBookingResponse getBooking(String query, String orderBy, Order order, int offset, int limit)
			throws Exception {
		final List<AssignedInmate> inmates = inmateRepository.findAllInmates(query, offset, limit, orderBy, order);
		InmateSummaries inmateSummaries = new InmateSummaries(inmates, MetaDataFactory.createMetaData(limit, offset, inmates));
		return GetBookingResponse.withJsonOK(inmateSummaries);
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
		
		List<CaseNote> caseNotes = caseNoteService.getCaseNotes(bookingId, query, orderBy, order, offset, limit);
		CaseNotes cases = new CaseNotes(caseNotes, MetaDataFactory.createMetaData(limit, offset, caseNotes));
		return GetBookingByBookingIdCaseNotesResponse.withJsonOK(cases);
	}

	@Override
	public PostBookingByBookingIdCaseNotesResponse postBookingByBookingIdCaseNotes(String bookingId, NewCaseNote newCaseNote) throws Exception {
		CaseNote caseNote = caseNoteService.createCaseNote(bookingId, newCaseNote);
		return PostBookingByBookingIdCaseNotesResponse.withJsonCreated(caseNote);
	}
	
	@Override
	public GetBookingByBookingIdCaseNotesByCaseNoteIdResponse getBookingByBookingIdCaseNotesByCaseNoteId(
			String bookingId, String caseNoteId) throws Exception {
		final CaseNote caseNote = caseNoteService.getCaseNote(bookingId, Long.valueOf(caseNoteId));
		return GetBookingByBookingIdCaseNotesByCaseNoteIdResponse.withJsonOK(caseNote);
	}
	
	@Override
	public GetBookingByBookingIdAlertsResponse getBookingByBookingIdAlerts(String bookingId, String orderBy,
			Order order, String query, int offset, int limit) throws Exception {
		List<Alert> alerts = inmateAlertService.getInmateAlerts(bookingId, query, orderBy, order, offset, limit);
		Alerts alertsObj = new Alerts(alerts, MetaDataFactory.createMetaData(limit, offset, alerts));
		return GetBookingByBookingIdAlertsResponse.withJsonOK(alertsObj);
	}
	
	@Override
	public GetBookingByBookingIdAlertsByAlertIdResponse getBookingByBookingIdAlertsByAlertId(String bookingId,
			String alertId) throws Exception {
		Alert alert = inmateAlertService.getInmateAlert(bookingId, alertId);
		return GetBookingByBookingIdAlertsByAlertIdResponse.withJsonOK(alert);
	}
	
	@Override
	public GetBookingByBookingIdAliasesResponse getBookingByBookingIdAliases(String bookingId, String orderBy,
			Order order, int offset, int limit) throws Exception {
		try {
			List<Alias> aliases = inmateRepository.findInmateAliases(Long.valueOf(bookingId), orderBy, order, offset, limit);
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
		CaseNote caseNote = caseNoteService.updateCaseNote(bookingId, Long.valueOf(caseNoteId), entity.getText());
		return PutBookingByBookingIdCaseNotesByCaseNoteIdResponse.withJsonCreated(caseNote);
	}


}

