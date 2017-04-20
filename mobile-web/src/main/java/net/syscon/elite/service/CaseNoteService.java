package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.resource.BookingResource.Order;

public interface CaseNoteService {
	
	List<CaseNote> getCaseNotes(String bookingId, String query,
			String orderBy, Order order, int offset, int limit) ;
	CaseNote getCaseNote(String bookingId, String caseNoteId);
	CaseNote createCaseNote(String bookingId, String caseNoteId, CaseNote entity);
	CaseNote updateCaseNote(String bookingId, String caseNoteId, CaseNote entity);
	

}
