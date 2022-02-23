package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "cell")
@RequestMapping("${api.base.path}/cell")
public class CellResource {

    private final BedAssignmentHistoryService bedAssignmentHistoryService;
    private final AgencyService agencyService;

    public CellResource(final BedAssignmentHistoryService bedAssignmentHistoryService, final AgencyService agencyService) {
        this.bedAssignmentHistoryService = bedAssignmentHistoryService;
        this.agencyService = agencyService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/{locationId}/history")
    public List<BedAssignment> getBedAssignmentsHistory(@PathVariable("locationId") @Parameter(description = "The location id.", required = true) final Long locationId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("fromDate") @Parameter(description = "From date", example = "2020-03-24T10:10:10", required = true) final LocalDateTime fromDateTime, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("toDate") @Parameter(description = "To date", example = "2020-12-01T11:11:11", required = true) final LocalDateTime toDateTime) {
        return bedAssignmentHistoryService.getBedAssignmentsHistory(locationId, fromDateTime, toDateTime);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/{agencyId}/history/{assignmentDate}")
    public List<BedAssignment> getBedAssignmentsHistoryByDateForAgency(
        @Parameter(description = "Agency Id", example = "MDI", required = true) @PathVariable("agencyId") final String agencyId,
        @DateTimeFormat(iso = ISO.DATE) @PathVariable("assignmentDate")
        @Parameter(description = "Assignment date (2020-03-24)", example = "2020-03-24", required = true) final LocalDate assignmentDate) {
        return bedAssignmentHistoryService.getBedAssignmentsHistoryByDateForAgency(agencyId, assignmentDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/{locationId}/attributes")
    public OffenderCell getCellAttributes(@PathVariable("locationId") @Parameter(description = "The location id.", required = true) final Long locationId) {
        return agencyService.getCellAttributes(locationId);
    }
}
