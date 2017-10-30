package net.syscon.elite.service;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

public interface CaseNoteService {
	Page<CaseNote> getCaseNotes(long bookingId, String query, String orderBy, Order order, long offset, long limit);
	CaseNote getCaseNote(long bookingId, long caseNoteId);
	CaseNote createCaseNote(long bookingId, NewCaseNote caseNote);
	CaseNote updateCaseNote(long bookingId, long caseNoteId, String newCaseNoteText);
}
