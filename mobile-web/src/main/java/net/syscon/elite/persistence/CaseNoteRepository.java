package net.syscon.elite.persistence;

import java.util.List;

import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import net.syscon.elite.web.api.resource.BookingResource.Order;

public interface CaseNoteRepository {
	
	List<CaseNote> getCaseNotes(String bookingId, String query,
			String orderBy, Order order, int offset, int limit) ;
	CaseNote getCaseNote(String bookingId, String CaseNoteId);
	CaseNote createCaseNote(String bookingId, String CaseNoteId, CaseNote entity);
	CaseNote updateCaseNote(String bookingId, String CaseNoteId, UpdateCaseNote entity);
	
}
