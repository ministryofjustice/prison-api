package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.validation.CaseNoteTypeSubTypeValid;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    List<CaseNoteUsage> getCaseNoteUsage(String type, String subType, List<String> offenderNo, Integer staffId, String agencyId, LocalDate fromDate, LocalDate toDate, int numMonths);

    List<CaseNoteUsageByBookingId> getCaseNoteUsageByBookingId(String type, String subType, @NotEmpty List<Integer> bookingIds, LocalDate fromDate, LocalDate toDate, int numMonths);

    List<CaseNoteStaffUsage> getCaseNoteStaffUsage(String type, String subType, @NotEmpty List<Integer> staffIds, LocalDate fromDate, LocalDate toDate, int numMonths);

    List<CaseNoteEvent> getCaseNotesEvents(@NotEmpty final List<String> noteTypes, @NotNull final LocalDateTime createdDate);

    List<CaseNoteEvent> getCaseNotesEvents(@NotEmpty List<String> noteTypes, @NotNull LocalDateTime createdDate, @NotNull @Min(1) @Max(5000) Long limit);
}
