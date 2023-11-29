package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderAttendance;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.OffenderActivitiesService;

import java.time.LocalDate;

@RestController
@Tag(name = "offender-activities")
@Validated
@RequestMapping(value = "${api.base.path}/offender-activities", produces = "application/json")
@PreAuthorize("hasRole('VIEW_ACTIVITIES')")
public class OffenderActivitiesResource {

    private final OffenderActivitiesService activitiesService;

    public OffenderActivitiesResource(final OffenderActivitiesService activitiesService) {
        this.activitiesService = activitiesService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "The activities that this offender has been allocated to.", description = "This includes suspended activities")
    @GetMapping("/{offenderNo}/activities-history")
    @SlowReportQuery
    public Page<OffenderActivitySummary> getRecentStartedActivities(@PathVariable("offenderNo") @Parameter(description = "The offenderNo of the prisoner", required = true) final String offenderNo,
                                                                    @RequestParam(value = "earliestEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only include activities that have not ended or have an end date after the given date", example = "1970-01-02") final LocalDate earliestEndDate,
                                                                    @ParameterObject @PageableDefault(size = 20) final Pageable pageable
    ) {
        return activitiesService.getStartedActivities(offenderNo, earliestEndDate, pageable);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "The activities that this offender attended over a time period.")
    @GetMapping("/{offenderNo}/attendance-history")
    @SlowReportQuery
    public Page<OffenderAttendance> getHistoricalAttendances(@PathVariable("offenderNo") @Parameter(description = "The offenderNo of the prisoner", required = true) final String offenderNo,
                                                             @RequestParam(value = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only include attendences on or after this date", example = "2021-01-02", required = true) final LocalDate earliestActivityDate,
                                                             @RequestParam(value = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only include attendences on or before this date", example = "2021-05-27", required = true) final LocalDate latestActivityDate,
                                                             @RequestParam(value = "outcome", required = false) @Parameter(description = "Only include attendences which have this outcome (default all)", schema = @Schema(implementation = String.class, allowableValues = {"ABS","ACCAB","ATT","CANC","NREQ","SUS","UNACAB","REST"})) final String outcome,
                                                             @ParameterObject @PageableDefault(size = 20) final Pageable pageable
    ) {
        return activitiesService.getHistoricalAttendancies(offenderNo, earliestActivityDate, latestActivityDate, outcome, pageable);
    }
}
