package net.syscon.elite.repository;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.support.Order;

import java.util.List;
import java.util.Optional;

public interface CaseNoteRepository {
	
	List<CaseNote> getCaseNotes(long bookingId, String query,
								String orderBy, Order order, long offset, long limit) ;
	Optional<CaseNote> getCaseNote(long bookingId, long caseNoteId);
	Long createCaseNote(long bookingId, NewCaseNote caseNote, String sourceCode);
	void updateCaseNote(long bookingId, long caseNoteId, String additionalCaseNoteText, String userId);
	
}
