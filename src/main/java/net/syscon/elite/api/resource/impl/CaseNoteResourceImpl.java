package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.CaseNoteResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CaseNoteService;
import org.apache.commons.lang3.ObjectUtils;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestResource
@Path("/case-notes")
public class CaseNoteResourceImpl implements CaseNoteResource {

    private final CaseNoteService caseNoteService;

    public CaseNoteResourceImpl(final CaseNoteService caseNoteService) {
        this.caseNoteService = caseNoteService;
    }

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
    public List<CaseNoteUsageByBookingId> getCaseNoteSummaryByBookingId(final List<Integer> bookingIds, final Integer numMonths, final LocalDate fromDate, final LocalDate toDate, final String type, final String subType) {
        return caseNoteService.getCaseNoteUsageByBookingId(type, subType, bookingIds, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }
}
