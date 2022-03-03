package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderAttendance;
import uk.gov.justice.hmpps.prison.service.OffenderActivitiesService;

import java.time.LocalDate;

@RestController
@Api(tags = {"offender-activities"})
@Validated
@RequestMapping("${api.base.path}/offender-activities")
public class OffenderActivitiesResource {

    private final OffenderActivitiesService activitiesService;

    public OffenderActivitiesResource(final OffenderActivitiesService activitiesService) {
        this.activitiesService = activitiesService;
    }

    @ApiResponses({
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The activities that this offender has been allocated to.", notes = "This includes suspended activities", nickname = "getRecentStartedActivities")
    @GetMapping("/{offenderNo}/activities-history")
    public Page<OffenderActivitySummary> getRecentStartedActivities(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of the prisoner", required = true) final String offenderNo,
                                                                    @RequestParam(value = "earliestEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Only include activities that have not ended or have an end date after the given date", example = "1970-01-02") final LocalDate earliestEndDate,
                                                                    @PageableDefault(page = 0, size = 20) final Pageable pageable
    ) {
        return activitiesService.getStartedActivities(offenderNo, earliestEndDate, pageable);
    }

    @ApiResponses({
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The activities that this offender attended over a time period.", nickname = "getHistoricalAttendances")
    @GetMapping("/{offenderNo}/attendance-history")
    public Page<OffenderAttendance> getHistoricalAttendances(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of the prisoner", required = true) final String offenderNo,
                                                             @RequestParam(value = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Only include attendences on or after this date", example = "2021-01-02", required = true) final LocalDate earliestActivityDate,
                                                             @RequestParam(value = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Only include attendences on or before this date", example = "2021-05-27", required = true) final LocalDate latestActivityDate,
                                                             @RequestParam(value = "outcome", required = false) @ApiParam(value = "Only include attendences which have this outcome (default all)", allowableValues = "ABS,ACCAB,ATT,CANC,NREQ,SUS,UNACAB,REST") final String outcome,
                                                             @PageableDefault(page = 0, size = 20) final Pageable pageable
    ) {
        return activitiesService.getHistoricalAttendancies(offenderNo, earliestActivityDate, latestActivityDate, outcome, pageable);
    }
}
