package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.service.AgencyService;
import uk.gov.justice.hmpps.prison.service.BedAssignmentHistoryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@Api(tags = {"cell"})
@RequestMapping("${api.base.path}/cell")
public class CellResource {

    private final BedAssignmentHistoryService bedAssignmentHistoryService;
    private final AgencyService agencyService;

    public CellResource(final BedAssignmentHistoryService bedAssignmentHistoryService, final AgencyService agencyService) {
        this.bedAssignmentHistoryService = bedAssignmentHistoryService;
        this.agencyService = agencyService;
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @GetMapping("/{locationId}/history")
    public List<BedAssignment> getBedAssignmentsHistory(@PathVariable("locationId") @ApiParam(value = "The location id.", required = true) final Long locationId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("fromDate") @ApiParam(value = "From date", example = "2020-03-24T10:10:10", required = true) final LocalDateTime fromDateTime, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("toDate") @ApiParam(value = "To date", example = "2020-12-01T11:11:11", required = true) final LocalDateTime toDateTime) {
        return bedAssignmentHistoryService.getBedAssignmentsHistory(locationId, fromDateTime, toDateTime);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @GetMapping("/{agencyId}/history/{assignmentDate}")
    public List<BedAssignment> getBedAssignmentsHistoryByDateForAgency(
        @ApiParam(value = "Agency Id", example = "MDI", required = true) @PathVariable("agencyId") final String agencyId,
        @DateTimeFormat(iso = ISO.DATE) @PathVariable("assignmentDate")
        @ApiParam(value = "Assignment date (2020-03-24)", example = "2020-03-24", required = true) final LocalDate assignmentDate) {
        return bedAssignmentHistoryService.getBedAssignmentsHistoryByDateForAgency(agencyId, assignmentDate);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @GetMapping("/{locationId}/attributes")
    public OffenderCell getCellAttributes(@PathVariable("locationId") @ApiParam(value = "The location id.", required = true) final Long locationId) {
        return agencyService.getCellAttributes(locationId);
    }
}
