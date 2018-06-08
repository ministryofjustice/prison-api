package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CaseNoteUsage;
import net.syscon.elite.api.resource.CaseNoteResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CaseNoteService;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;

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
}
