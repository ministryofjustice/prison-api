package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.NewCaseNote;
import net.syscon.elite.web.api.resource.BookingResource.Order;

import java.util.List;

public interface CaseNoteRepository {
	
	List<CaseNote> getCaseNotes(String bookingId, String query,
			String orderBy, Order order, int offset, int limit) ;
	CaseNote getCaseNote(String bookingId, long caseNoteId);
	Long createCaseNote(String bookingId, NewCaseNote caseNote, String sourceCode);
	void updateCaseNote(String bookingId, long caseNoteId, String additionalCaseNoteText, String userId);
	
}
