package net.syscon.elite.persistence;

import net.syscon.elite.v2.api.model.CaseNote;
import net.syscon.elite.v2.api.model.NewCaseNote;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;
import java.util.Optional;

public interface CaseNoteRepository {
	
	List<CaseNote> getCaseNotes(String bookingId, String query,
								String orderBy, Order order, long offset, long limit) ;
	Optional<CaseNote> getCaseNote(long bookingId, long caseNoteId);
	Long createCaseNote(long bookingId, NewCaseNote caseNote, String sourceCode);
	void updateCaseNote(long bookingId, long caseNoteId, String additionalCaseNoteText, String userId);
	
}
