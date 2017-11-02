package net.syscon.elite.service;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.validation.ReferenceCodesValid;

import javax.validation.Valid;

import java.time.LocalDate;

public interface CaseNoteService {
    Page<CaseNote> getCaseNotes(long bookingId, String query, LocalDate from, LocalDate to, String orderBy, Order order,
            long offset, long limit);
    CaseNote getCaseNote(long bookingId, long caseNoteId);
    CaseNote createCaseNote(long bookingId, @Valid @ReferenceCodesValid NewCaseNote caseNote);
    CaseNote updateCaseNote(long bookingId, long caseNoteId, @Valid String newCaseNoteText);
}
