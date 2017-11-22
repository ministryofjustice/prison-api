package net.syscon.elite.repository;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;
import java.util.Optional;

public interface CaseNoteRepository {
    Page<CaseNote> getCaseNotes(long bookingId, String query, LocalDate from, LocalDate to, String orderBy, Order order, long offset, long limit);

    Optional<CaseNote> getCaseNote(long bookingId, long caseNoteId);

    Long createCaseNote(long bookingId, NewCaseNote caseNote, String sourceCode);

    void updateCaseNote(long bookingId, long caseNoteId, String additionalCaseNoteText, String userId);

    Long getCaseNoteCount(long bookingId, String type, String subType, LocalDate fromDate, LocalDate toDate);
}
