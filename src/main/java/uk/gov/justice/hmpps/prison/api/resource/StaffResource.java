package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.api.model.StaffLocationRole;
import uk.gov.justice.hmpps.prison.api.model.StaffRole;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess;
import uk.gov.justice.hmpps.prison.security.VerifyStaffAccess;
import uk.gov.justice.hmpps.prison.service.StaffJobType;
import uk.gov.justice.hmpps.prison.service.StaffService;
import uk.gov.justice.hmpps.prison.service.support.GetStaffRoleRequest;

import java.util.List;

@RestController
@Tag(name = "staff")
@Validated
@RequestMapping(value = "${api.base.path}/staff", produces = "application/json")
public class StaffResource {
    private final StaffService staffService;

    public StaffResource(final StaffService staffService) {
        this.staffService = staffService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema=@Schema(implementation = StaffDetail.class)))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Retrieve details for multiple staff members using ids.", description = "Security note: staff details are only available for the current user unless client has ROLE_STAFF_SEARCH.")
    @VerifyStaffAccess(overrideRoles = {"STAFF_SEARCH"})
    @PostMapping
    public List<StaffDetail> getStaffDetails(@RequestBody @NotEmpty List<Long> staffIds) {
        return staffService.getStaffDetails(staffIds);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StaffDetail.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Staff detail.", description = "Security note: staff details are only available for the current user unless client has ROLE_STAFF_SEARCH.")
    @VerifyStaffAccess(overrideRoles = {"STAFF_SEARCH"})
    @GetMapping("/{staffId}")
    public StaffDetail getStaffDetail(@PathVariable("staffId") @Parameter(description = "The staff id of the staff member.", required = true) final Long staffId) {
        return staffService.getStaffDetail(staffId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "The staffId supplied was not valid.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "204", description = "No email addresses were found for this staff member."),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Returns a list of email addresses associated with this staff user", description = "Security note: staff details are only available for the current user unless client has ROLE_STAFF_SEARCH.")
    @VerifyStaffAccess(overrideRoles = {"STAFF_SEARCH"})
    @GetMapping("/{staffId}/emails")
    public List<String> getStaffEmailAddresses(@PathVariable("staffId") @Parameter(description = "The staff id of the staff user.", required = true) final Long staffId) {
        return staffService.getStaffEmailAddresses(staffId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "The staffId supplied was not valid or not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "204", description = "No caseloads were found for this staff member."),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Returns a list of caseloads associated with this staff user", description = "Security note: staff details are only available for the current user unless client has ROLE_STAFF_SEARCH.")
    @VerifyStaffAccess(overrideRoles = {"STAFF_SEARCH"})
    @GetMapping("/{staffId}/caseloads")
    public List<CaseLoad> getStaffCaseloads(@PathVariable("staffId") @Parameter(description = "The staff id of the staff user.", required = true, example = "123123") final Long staffId) {
        return staffService.getStaffCaseloads(staffId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get staff members within agency who are currently assigned the specified role.", description = "Get staff members within agency who are currently assigned the specified role. Security note: the agency must be in the current user's caseload.")
    @VerifyAgencyAccess(overrideRoles = {"STAFF_SEARCH"})
    @GetMapping("/roles/{agencyId}/role/{role}")
    @SlowReportQuery
    public ResponseEntity<List<StaffLocationRole>> getStaffByAgencyRole(
        @PathVariable("agencyId") @Parameter(description = "The agency (prison) id.", required = true) final String agencyId,
        @PathVariable("role") @Parameter(description = "The staff role.", required = true) final String role,
        @RequestParam(value = "nameFilter", required = false) @Parameter(description = "Filter results by first name and/or last name of staff member. Supplied filter term is matched to start of staff member's first and last name.") final String nameFilter,
        @RequestParam(value = "staffId", required = false) @Parameter(description = "The staff id of a staff member.") final Long staffId,
        @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @Parameter(description = "Filters results by activeOnly staff members.") final Boolean activeOnly,
        @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of role records.") final Long pageOffset,
        @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of role records returned.") final Long pageLimit,
        @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") final String sortFields,
        @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder
    ) {
        final var defaultedActiveOnly = activeOnly != null ? activeOnly : Boolean.TRUE;

        final var staffRoleRequest = new GetStaffRoleRequest(agencyId, null, role, nameFilter, defaultedActiveOnly, staffId);
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return ResponseEntity.ok()
            .headers(staffDetails.getPaginationHeaders())
            .body(staffDetails.getItems());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of job roles for specified staff and agency Id", description = "Security note: the agency must be in the current user's caseload.")
    @VerifyAgencyAccess(overrideRoles = {"STAFF_SEARCH"}, allowInactive = true)
    @GetMapping("/{staffId}/{agencyId}/roles")
    public List<StaffRole> getAllRolesForAgency(
        @PathVariable("agencyId") @Parameter(description = "Agency Id.", required = true) final String agencyId,
        @PathVariable("staffId") @Parameter(description = "The staff id of the staff member.", required = true) final Long staffId
    ) {
        return staffService.getAllRolesForAgency(staffId, agencyId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Check if staff member has a role", description = "Check if staff member has a role, either KW or POM. Security note: the agency must be in the current user's caseload.")
    @VerifyAgencyAccess(overrideRoles = {"STAFF_SEARCH"}, allowInactive = true)
    @GetMapping("/{staffId}/{agencyId}/roles/{roleType}")
    public boolean hasStaffRole(
        @PathVariable("agencyId") @Parameter(description = "Agency Id.", required = true, example = "MDI") @NotNull final String agencyId,
        @PathVariable("staffId") @Parameter(description = "The staff id of the staff member.", required = true, example = "1111111") @NotNull final Long staffId,
        @PathVariable("roleType") @Parameter(description = "Type of role", required = true, example = "KW") @NotNull final StaffJobType roleType
    ) {
        return staffService.hasStaffRole(staffId, agencyId, roleType);
    }
}
