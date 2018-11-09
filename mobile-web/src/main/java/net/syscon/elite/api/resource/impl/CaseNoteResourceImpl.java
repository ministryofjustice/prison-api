package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CaseNoteStaffUsage;
import net.syscon.elite.api.model.CaseNoteStaffUsageRequest;
import net.syscon.elite.api.model.CaseNoteUsage;
import net.syscon.elite.api.model.CaseNoteUsageRequest;
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

    public CaseNoteResourceImpl(CaseNoteService caseNoteService) {
        this.caseNoteService = caseNoteService;
    }

    @Override
    public GetCaseNoteStaffUsageSummaryResponse getCaseNoteStaffUsageSummary(List<String> staffIds, Integer numMonths, LocalDate fromDate, LocalDate toDate, String type, String subType) {
        List<Integer> staffIdList = staffIds.stream().map(Integer::valueOf).collect(Collectors.toList());
        List<CaseNoteStaffUsage> caseNoteStaffUsage = caseNoteService.getCaseNoteStaffUsage(type, subType, staffIdList, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));

        return GetCaseNoteStaffUsageSummaryResponse.respond200WithApplicationJson(caseNoteStaffUsage);
    }

    @Override
    public GetCaseNoteUsageSummaryResponse getCaseNoteUsageSummary(List<String> offenderNo, Integer staffId, Integer numMonths, LocalDate fromDate, LocalDate toDate, String type, String subType) {
        List<CaseNoteUsage> caseNoteUsage = caseNoteService.getCaseNoteUsage(type, subType, offenderNo, staffId, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, ObjectUtils.defaultIfNull(numMonths, 1)));

        return GetCaseNoteUsageSummaryResponse.respond200WithApplicationJson(caseNoteUsage);
    }

    @Override
    public GetCaseNoteStaffUsageSummaryByPostResponse getCaseNoteStaffUsageSummaryByPost(CaseNoteStaffUsageRequest request) {
        List<CaseNoteStaffUsage> caseNoteStaffUsage = caseNoteService.getCaseNoteStaffUsage(request.getType(), request.getSubType(), request.getStaffIds(), request.getFromDate(), request.getToDate(), ObjectUtils.defaultIfNull(request.getNumMonths(), 1));

        return GetCaseNoteStaffUsageSummaryByPostResponse.respond200WithApplicationJson(caseNoteStaffUsage);
    }

    @Override
    public GetCaseNoteUsageSummaryByPostResponse getCaseNoteUsageSummaryByPost(CaseNoteUsageRequest request) {
        List<CaseNoteUsage> caseNoteUsage = caseNoteService.getCaseNoteUsage(request.getType(), request.getSubType(), request.getOffenderNos(), request.getStaffId(), request.getFromDate(), request.getToDate(), ObjectUtils.defaultIfNull(request.getNumMonths(), 1));

        return GetCaseNoteUsageSummaryByPostResponse.respond200WithApplicationJson(caseNoteUsage);
    }
}
