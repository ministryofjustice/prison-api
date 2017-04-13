package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.web.api.model.Casenote;
import net.syscon.elite.web.api.resource.BookingResource.Order;

public interface CaseNoteService {
	
	List<Casenote> getCaseNotes(String bookingId, String query,
			String orderBy, Order order, int offset, int limit) ;
	Casenote getCaseNote(String bookingId, String caseNoteId);
	Casenote createCaseNote(String bookingId, String caseNoteId, Casenote entity);
	Casenote updateCaseNote(String bookingId, String caseNoteId, Casenote entity);
	

}
