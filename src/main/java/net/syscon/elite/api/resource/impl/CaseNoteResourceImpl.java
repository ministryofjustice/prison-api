package net.syscon.elite.api.resource.impl;

import lombok.AllArgsConstructor;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.CaseNoteResource;
import net.syscon.elite.service.CaseNoteService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/case-notes")
@AllArgsConstructor
public class CaseNoteResourceImpl implements CaseNoteResource {

    private final CaseNoteService caseNoteService;

    @Override
    public List<CaseNoteStaffUsage> getCaseNoteStaffUsageSummary(final List<String> staffIds, final Integer numMonths, final LocalDate fromDate, final LocalDate toDate, final String type, final String subType) {
        final var staffIdList = staffIds.stream().map(Integer::valueOf).collect(Collectors.toList());
        return caseNoteService.getCaseNoteStaffUsage(type, subType, staffIdList, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }

    @Override
    public List<CaseNoteStaffUsage> getCaseNoteStaffUsageSummaryByPost(final CaseNoteStaffUsageRequest request) {
        return caseNoteService.getCaseNoteStaffUsage(request.getType(), request.getSubType(), request.getStaffIds(), request.getFromDate(), request.getToDate(), ObjectUtils.defaultIfNull(request.getNumMonths(), 1));
    }

    @Override
    public List<CaseNoteUsage> getCaseNoteUsageSummary(final List<String> offenderNo, final Integer staffId, final String agencyId, final Integer numMonths, final LocalDate fromDate, final LocalDate toDate, final String type, final String subType) {
        return caseNoteService.getCaseNoteUsage(type, subType, offenderNo, staffId, agencyId, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }

    @Override
    public List<CaseNoteUsage> getCaseNoteUsageSummaryByPost(final CaseNoteUsageRequest request) {
        return caseNoteService.getCaseNoteUsage(request.getType(), request.getSubType(), request.getOffenderNos(), request.getStaffId(), request.getAgencyId(), request.getFromDate(), request.getToDate(), ObjectUtils.defaultIfNull(request.getNumMonths(), 1));
    }

    @Override
    public List<CaseNoteEvent> getCaseNotesEventsNoLimit(final List<String> noteTypes, final LocalDateTime createdDate) {
        return caseNoteService.getCaseNotesEvents(noteTypes, createdDate);
    }

    @Override
    public List<CaseNoteEvent> getCaseNotesEvents(final List<String> noteTypes, final LocalDateTime createdDate, final Long limit) {
        return caseNoteService.getCaseNotesEvents(noteTypes, createdDate, limit);
    }

    @Override
    public List<CaseNoteUsageByBookingId> getCaseNoteSummaryByBookingId(final List<Integer> bookingIds, final Integer numMonths, final LocalDate fromDate, final LocalDate toDate, final String type, final String subType) {
        return caseNoteService.getCaseNoteUsageByBookingId(type, subType, bookingIds, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }
}
