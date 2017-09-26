package net.syscon.elite.service;

import net.syscon.elite.v2.api.model.CaseNote;
import net.syscon.elite.v2.api.model.NewCaseNote;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;

public interface CaseNoteService {
	
	List<CaseNote> getCaseNotes(String bookingId, String query,
								String orderBy, Order order, int offset, int limit) ;
	CaseNote getCaseNote(String bookingId, long caseNoteId);
	CaseNote createCaseNote(String bookingId, NewCaseNote caseNote);
	CaseNote updateCaseNote(String bookingId, long caseNoteId, String newCaseNoteText);

}
