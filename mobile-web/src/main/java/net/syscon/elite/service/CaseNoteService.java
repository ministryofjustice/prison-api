package net.syscon.elite.service;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.CaseNoteCount;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.validation.CaseNoteTypeSubTypeValid;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

public interface CaseNoteService {
    Page<CaseNote> getCaseNotes(Long bookingId, String query, LocalDate from, LocalDate to, String orderBy, Order order, long offset, long limit);

    CaseNote getCaseNote(Long bookingId, Long caseNoteId);

    CaseNote createCaseNote(Long bookingId, @Valid @CaseNoteTypeSubTypeValid NewCaseNote caseNote, String username);

    CaseNote updateCaseNote(Long bookingId, Long caseNoteId, String username, @NotBlank(message="{caseNoteTextBlank}") @Length(max=4000, message="{caseNoteTextTooLong}") String newCaseNoteText);

    CaseNoteCount getCaseNoteCount(Long bookingId, String type, String subType, LocalDate fromDate, LocalDate toDate);

    List<ReferenceCode> getCaseNoteTypesByCaseLoadType(String caseLoadType);

    List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(String caseLoadType);

    List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes();
}
