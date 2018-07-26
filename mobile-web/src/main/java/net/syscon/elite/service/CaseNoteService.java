package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.validation.CaseNoteTypeSubTypeValid;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public interface CaseNoteService {
    Page<CaseNote> getCaseNotes(Long bookingId, String query, LocalDate from, LocalDate to, String orderBy, Order order, long offset, long limit);

    CaseNote getCaseNote(Long bookingId, Long caseNoteId);

    CaseNote createCaseNote(Long bookingId, @NotNull @Valid @CaseNoteTypeSubTypeValid NewCaseNote caseNote, String username);

    CaseNote updateCaseNote(Long bookingId, Long caseNoteId, String username, @NotBlank(message="{caseNoteTextBlank}") String newCaseNoteText);

    CaseNoteCount getCaseNoteCount(Long bookingId, String type, String subType, LocalDate fromDate, LocalDate toDate);

    List<ReferenceCode> getCaseNoteTypesByCaseLoadType(String caseLoadType);

    List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(String caseLoadType);

    List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes();

    List<CaseNoteUsage> getCaseNoteUsage(String type, String subType, @NotEmpty List<String> offenderNo, LocalDate fromDate, LocalDate toDate, int numMonths);

    List<CaseNoteStaffUsage> getCaseNoteStaffUsage(String type, String subType, @NotEmpty List<Integer> staffIds, LocalDate fromDate, LocalDate toDate, int numMonths);
}
