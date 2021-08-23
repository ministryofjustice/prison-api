package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderAttendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Attendance;
import uk.gov.justice.hmpps.prison.service.WIPOffenderActivitiesService;

import java.time.LocalDate;
import java.util.List;

@Hidden // This is WIP
@RestController
@Api(tags = {"offender-activities"})
@Validated
@RequestMapping("${api.base.path}/offender-activities")
public class WIPOffenderActivitiesResource {

    private final WIPOffenderActivitiesService activitesService;

    public WIPOffenderActivitiesResource(final WIPOffenderActivitiesService activitesService) {
        this.activitesService = activitesService;
    }

    @ApiResponses({
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The activities that this offender was scheduled to attend over a time period", nickname = "getCurrentWorkActivities")
    @GetMapping("/{offenderNo}/activity-history")
    public List<OffenderAttendance> getHistoricalActivityAttendances(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of the prisoner", required = true) final String offenderNo,
                                                                     @RequestParam(value = "fromDate") @DateTimeFormat(iso = ISO.DATE) @ApiParam(value = "Only include scheduled activities on or after this date", example = "1970-01-02") final LocalDate earliestActivityDate,
                                                                     @RequestParam(value = "toDate") @DateTimeFormat(iso = ISO.DATE) @ApiParam(value = "Only include scheduled activities on or before this date", example = "1970-01-02") final LocalDate latestActivityDate) {
        return activitesService.getHistoricalActivities(offenderNo, earliestActivityDate, latestActivityDate);
    }
}
