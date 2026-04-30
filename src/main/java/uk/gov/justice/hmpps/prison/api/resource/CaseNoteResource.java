package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeCount;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;

import java.util.List;

@RestController
@Validated
@Tag(name = "case-notes")
@RequestMapping(value = "${api.base.path}/case-notes", produces = "application/json")
@AllArgsConstructor
public class CaseNoteResource {

    private final CaseNoteService caseNoteService;

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The case note usage list is returned.")})
    @Operation(
        deprecated = true,
        summary = "This endpoint has been replaced in the case notes service - see case-notes-service/case-notes/usage"
    )
    @PostMapping("/usage-by-types")
    @SlowReportQuery
    @PreAuthorize("hasRole('VIEW_CASE_NOTES')")
    public List<CaseNoteTypeCount> getCaseNoteUsageSummaryByDates(@RequestBody @Parameter(required = true) final CaseNoteTypeSummaryRequest request) {
        return caseNoteService.getCaseNoteUsageByBookingIdTypeAndDate(request.getTypes(), request.getBookingFromDateSelection());
    }
}
