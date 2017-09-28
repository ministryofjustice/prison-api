package net.syscon.elite.service;

import net.syscon.elite.v2.api.model.CaseNote;
import net.syscon.elite.v2.api.model.NewCaseNote;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;

public interface CaseNoteService {
	
	List<CaseNote> getCaseNotes(long bookingId, String query,
								String orderBy, Order order, long offset, long limit) ;
	CaseNote getCaseNote(long bookingId, long caseNoteId);
	CaseNote createCaseNote(long bookingId, NewCaseNote caseNote);
	CaseNote updateCaseNote(long bookingId, long caseNoteId, String newCaseNoteText);

}
