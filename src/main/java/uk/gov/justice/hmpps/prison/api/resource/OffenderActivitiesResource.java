package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivities;
import uk.gov.justice.hmpps.prison.service.OffenderActivitiesService;

import java.time.LocalDate;

@Hidden // This is WIP
@RestController
@Api(tags = {"offender-relationships"})
@Validated
@RequestMapping("${api.base.path}/offender-activities")
public class OffenderActivitiesResource {

    private final OffenderActivitiesService activitesService;

    public OffenderActivitiesResource(final OffenderActivitiesService activitesService) {
        this.activitesService = activitesService;
    }

    @ApiResponses({
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The work activities that this offender is currently allocated to", notes = "This includes suspended activities", nickname = "getCurrentWorkActivities")
    @GetMapping("/{offenderNo}/current-work")
    public OffenderActivities getCurrentWorkActivities(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of the prisoner", required = true) final String offenderNo) {
        return activitesService.getCurrentWorkActivities(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The work activities that this offender has been allocated to", notes = "This includes suspended activities", nickname = "getRecentStartedWorkActivities")
    @GetMapping("/{offenderNo}/work-history")
    public OffenderActivities getRecentStartedWorkActivities(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of the prisoner", required = true) final String offenderNo,
            @RequestParam(value = "earliestEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Only include activities that have not ended or have an end date after the given date", example = "1970-01-02") final LocalDate earliestEndDate) {
        return activitesService.getStartedWorkActivities(offenderNo, earliestEndDate);
    }
}
