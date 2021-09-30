package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivities;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.service.OffenderActivitiesService;

import java.time.LocalDate;

import static java.util.Optional.ofNullable;

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
    @ApiOperation(value = "The work activities that this offender has been allocated to", notes = "This includes suspended activities", nickname = "getRecentStartedWorkActivities")
    @GetMapping("/{offenderNo}/activities-history")
    public Page<OffenderActivitySummary> getRecentStartedActivities(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of the prisoner", required = true) final String offenderNo,
                                                                        @RequestParam(value = "earliestEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Only include activities that have not ended or have an end date after the given date", example = "1970-01-02") final LocalDate earliestEndDate,
                                                                        @RequestParam(value = "page", required = false) @ApiParam(value = "Target page number, zero being the first page", defaultValue = "0") final Integer pageIndex,
                                                                        @RequestParam(value = "size", required = false) @ApiParam(value = "The number of results per page", defaultValue = "20") final Integer pageSize) {
        final var pageIndexValue = ofNullable(pageIndex).orElse(0);
        final var pageSizeValue = ofNullable(pageSize).orElse(20);
        final PageRequest pageRequest = PageRequest.of(pageIndexValue, pageSizeValue);

        return activitiesService.getStartedActivities(offenderNo, earliestEndDate, pageRequest);
    }
}
