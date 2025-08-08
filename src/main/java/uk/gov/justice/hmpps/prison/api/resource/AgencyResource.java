package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentTypes;
import uk.gov.justice.hmpps.prison.api.model.AgencyPrisonerPayProfile;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.LocationGroup;
import uk.gov.justice.hmpps.prison.api.model.LocationSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestToCreateAgency;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateAgency;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.ReferenceData;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.AgencyPrisonerPayProfileService;
import uk.gov.justice.hmpps.prison.service.AgencyService;
import uk.gov.justice.hmpps.prison.service.LocationGroupService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ACTIVE_ONLY;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

@RestController
@Validated
@Tag(name = "agencies")
@RequestMapping(value = "${api.base.path}/agencies", produces = "application/json")
public class AgencyResource {
    private final AgencyService agencyService;
    private final LocationGroupService locationGroupService;
    private final AgencyPrisonerPayProfileService agencyPrisonerPayProfileService;

    public AgencyResource(
        final AgencyService agencyService,
        final LocationGroupService locationGroupService,
        final AgencyPrisonerPayProfileService agencyPrisonerPayProfileService
    ) {
        this.agencyService = agencyService;
        this.locationGroupService = locationGroupService;
        this.agencyPrisonerPayProfileService = agencyPrisonerPayProfileService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of active agencies.", description = "List of active agencies.")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping
    public ResponseEntity<List<Agency>> getAgencies(@RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of agency records.") final Long pageOffset,
                                                    @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of agency records returned.") final Long pageLimit) {
        return agencyService.getAgencies(pageOffset, pageLimit).getResponse();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of agencies by type", description = "List of active agencies by type")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/type/{type}")
    public List<Agency> getAgenciesByType(
        @PathVariable("type")
        @Parameter(description = "Agency Type", required = true) final String agencyType,

        @RequestParam(value = "activeOnly", defaultValue = "true", required = false)
        @Parameter(description = "Only return active agencies")
        boolean activeOnly,

        @Deprecated(forRemoval = true)
        @RequestParam(value = "jurisdictionCode", required = false)
        @Parameter(description = "Only return agencies that match the supplied Jurisdiction Code(s), NOTE: Deprecated, please use courtType param", example = "MC")
        List<String> jurisdictionCodes,

        @RequestParam(value = "courtType", required = false)
        @Parameter(description = "Only return courts that match the supplied court types(s)", example = "MC")
        List<String> courtTypes,

        @RequestParam(value = "areaCode", required = false)
        @Parameter(description = "Only agencies that match the area code", example = "LONDON")
        String areaCode,

        @RequestParam(value = "regionCode", required = false)
        @Parameter(description = "Only agencies that match the region code", example = "LON")
        String regionCode,

        @RequestParam(value = "establishmentType", required = false)
        @Parameter(description = "Only agencies that match the establishment type", example = "CNOMIS")
        String establishmentType,

        @RequestParam(value = "withAddresses", defaultValue = "false", required = false)
        @Parameter(description = "Returns Address Information") final boolean withAddresses,

        @RequestParam(value = "skipFormatLocation", defaultValue = "false", required = false)
        @Parameter(description = "Don't format the location") final boolean skipFormatLocation,

        @RequestParam(value = "withAreas", defaultValue = "false", required = false)
        @Parameter(description = "Returns area and region information") final boolean withAreas
    ) {
        return agencyService.getAgenciesByType(agencyType, activeOnly, courtTypes != null ? courtTypes : jurisdictionCodes, areaCode, regionCode, establishmentType, skipFormatLocation, withAddresses, withAreas);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Agency detail.", description = "Agency detail.")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/{agencyId}")
    public Agency getAgency(
        @PathVariable("agencyId") @Parameter(description = "The ID of the agency", required = true) final String agencyId,
        @RequestParam(value = "activeOnly", defaultValue = "true", required = false) @Parameter(description = "Only return active agencies") final boolean activeOnly,
        @RequestParam(value = "agencyType", required = false) @Parameter(description = "Agency Type") final String agencyType,
        @RequestParam(value = "withAddresses", defaultValue = "false", required = false) @Parameter(description = "Returns Address Information") final boolean withAddresses,
        @RequestParam(value = "skipFormatLocation", defaultValue = "false", required = false) @Parameter(description = "Don't format the location") final boolean skipFormatLocation,
        @RequestParam(value = "withAreas", defaultValue = "false", required = false) @Parameter(description = "Returns area and region information") final boolean withAreas) {
        return agencyService.getAgency(agencyId, activeOnly ? ACTIVE_ONLY : ALL, agencyType, withAddresses, skipFormatLocation, withAreas);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to update a agency location", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update an existing agency", description = "Requires MAINTAIN_REF_DATA")
    @PutMapping("/{agencyId}")
    @PreAuthorize("hasRole('MAINTAIN_REF_DATA') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public Agency updateAgency(
        @PathVariable("agencyId") @Parameter(description = "The ID of the agency", required = true) @Valid @Length(max = 6, message = "Agency Id is max 6 characters") final String agencyId,
        @RequestBody @NotNull @Valid RequestToUpdateAgency agencyToUpdate) {
        return agencyService.updateAgency(agencyId, agencyToUpdate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "The Agency location created"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to create an agency location", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Create an agency", description = "Requires MAINTAIN_REF_DATA")
    @PostMapping()
    @PreAuthorize("hasRole('MAINTAIN_REF_DATA') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public ResponseEntity<Agency> createAgency(@RequestBody @NotNull @Valid final RequestToCreateAgency agencyToCreate) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.createAgency(agencyToCreate));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of receptions with capacity for agency.", description = "List of active receptions with capacity for agency.")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/{agencyId}/receptionsWithCapacity")
    @SlowReportQuery
    public List<OffenderCell> getAgencyActiveReceptionsWithCapacity(
        @PathVariable("agencyId") @Parameter(required = true) final String agencyId,
        @RequestParam(value = "attribute", required = false) @Parameter(description = "Restricts list of receptions returned to those that have a specified attribute.") final String attribute
    ) {
        return agencyService.getReceptionsWithCapacityInAgency(agencyId, attribute);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of all available Location Groups at agency.", description = "List of all available Location Groups at agency.")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/{agencyId}/locations/groups")
    @Deprecated(forRemoval = true)
    public List<LocationGroup> getAvailableLocationGroups(@PathVariable("agencyId") @Parameter(description = "The prison", required = true) final String agencyId) {
        return locationGroupService.getLocationGroupsForAgency(agencyId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of locations for agency where events (appointments, visits, activities) could be held.", description = "*** DEPRECATED *** List of locations for agency where events (appointments, visits, activities) could be held.")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/{agencyId}/eventLocations")
    @Deprecated(forRemoval = true)
    public List<Location> getAgencyEventLocations(
        @PathVariable("agencyId") @Parameter(required = true) final String agencyId,
        @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>description, userDescription</b>") final String sortFields,
        @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder
    ) {
        return agencyService.getAgencyEventLocations(agencyId, sortFields, sortOrder);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of location summaries for agency where events (appointments, visits, activities) are being held.",
        description = "List of location summaries for agency where events (appointments, visits, activities) are being held.")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/{agencyId}/eventLocationsBooked")
    @SlowReportQuery
    public List<LocationSummary> getAgencyEventLocationsBooked(
        @PathVariable("agencyId") @Parameter(required = true)
        final String agencyId,
        @RequestParam("bookedOnDay") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Parameter(description = "Filter list to only return locations which prisoners will be attending on this day", required = true)
        final LocalDate date,
        @RequestParam(value = "timeSlot", required = false)
        @Parameter(description = "Only return locations which prisoners will be attending in this time slot (AM, PM or ED, and bookedOnDay must be specified)", schema = @Schema(implementation = String.class, allowableValues = {"AM", "PM", "ED"}))
        final TimeSlot timeSlot
    ) {
        return agencyService.getAgencyEventLocationsBooked(agencyId, date, timeSlot);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of prison contact details.", description = """
        <p>List of prison contact details.</p>
        <p>DEPRECATED. This endpoint is quite slow as it currently retrieves address and telephone information for each prison separately.
        In all the main usages of the endpoint we found that the clients didn't need or use the contact details so have deprecated the endpoint.
        Use /agencies/prisons to get the list of active prisons.</p>
        """, deprecated = true)
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/prison")
    @Deprecated
    @SlowReportQuery
    public List<PrisonContactDetail> getPrisonContactDetailList() {
        return agencyService.getPrisonContactDetail();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of all active prisons.", description = """
        <p>List of active prisons.</p>
        <p>This is the same response as normally generated by calling the /agencies/type/INST endpoint with default parameters, added here for ease of use and speed.</p>
        """)
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/prisons")
    @SlowReportQuery
    public List<Prison> getPrisons() {
        return agencyService.getAgenciesByType("INST", true, null, null, null, null, false, false, false)
            .stream()
            .map((a -> new Prison(a.getAgencyId(), a.getDescription(), a.getLongDescription(), a.getAgencyType(), a.isActive())))
            .sorted(Comparator.comparing(Prison::getDescription, Comparator.naturalOrder()))
            .toList();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Prison contact detail.", description = "Prison contact detail.")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/prison/{agencyId}")
    public PrisonContactDetail getPrisonContactDetail(@PathVariable("agencyId") @Parameter(required = true) final String agencyId) {
        return agencyService.getPrisonContactDetail(agencyId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @Operation(summary = "Return the establishment types for the given Agency.", description = "An agency can have one to many establishment types. For example a prison could be both a youth and adult establishment.")
    @GetMapping("/{agencyId}/establishment-types")
    public AgencyEstablishmentTypes getAgencyEstablishmentTypes(@PathVariable("agencyId") @Parameter(required = true) final String agencyId) {
        return agencyService.getEstablishmentTypes(agencyId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return the payment profile data for the given Agency.", description = "Each agency can configure its own pay profile and this endpoint provides its key data, such as min/max pay and bonus rates. Requires VIEW_PRISON_DATA.")
    @PreAuthorize("hasRole('VIEW_PRISON_DATA')")
    @GetMapping("/{agencyId}/pay-profile")
    public AgencyPrisonerPayProfile getAgencyPayProfile(@PathVariable("agencyId") @Parameter(required = true) final String agencyId) {
        return agencyPrisonerPayProfileService.getAgencyPrisonerPayProfile(agencyId);
    }
}
