package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CaseNoteStaffUsage;
import net.syscon.elite.api.model.CaseNoteUsage;
import net.syscon.elite.api.resource.CaseNoteResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CaseNoteService;

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
    public GetCaseNoteUsageSummaryResponse getCaseNoteUsageSummary(List<String> offenderNo, LocalDate fromDate, LocalDate toDate, String type, String subType) {
        List<CaseNoteUsage> caseNoteUsage = caseNoteService.getCaseNoteUsage(type, subType, offenderNo, fromDate, toDate);

        return GetCaseNoteUsageSummaryResponse.respond200WithApplicationJson(caseNoteUsage);
    }

    @Override
    public GetCaseNoteStaffUsageSummaryResponse getCaseNoteStaffUsageSummary(List<String> staffId, LocalDate fromDate, LocalDate toDate, String type, String subType) {
        List<Integer> staffIds = staffId.stream().map(Integer::valueOf).collect(Collectors.toList());
        List<CaseNoteStaffUsage> caseNoteStaffUsage = caseNoteService.getCaseNoteStaffUsage(type, subType, staffIds, fromDate, toDate);

        return GetCaseNoteStaffUsageSummaryResponse.respond200WithApplicationJson(caseNoteStaffUsage);
    }

}
