package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CaseNoteRepository {

    Page<CaseNote> getCaseNotes(long bookingId, String query, LocalDate from, LocalDate to, String orderBy, Order order, long offset, long limit);

    Optional<CaseNote> getCaseNote(long bookingId, long caseNoteId);

    Long createCaseNote(long bookingId, NewCaseNote caseNote, String sourceCode, String username, Long staffId);

    void updateCaseNote(long bookingId, long caseNoteId, @Length(max = 4000, message = "{caseNoteTextTooLong}") String updatedText, String userId);

    Long getCaseNoteCount(long bookingId, String type, String subType, LocalDate fromDate, LocalDate toDate);

    List<ReferenceCode> getCaseNoteTypesByCaseLoadType(String caseLoadType);

    List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(String caseLoadType);

    List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes();

    List<CaseNoteUsage> getCaseNoteUsage(String type, String subType, List<String> offenderNos, LocalDate fromDate, LocalDate toDate);

    List<CaseNoteStaffUsage> getCaseNoteStaffUsage(String type, String subType, List<Integer> staffIds, LocalDate fromDate, LocalDate toDate);
}