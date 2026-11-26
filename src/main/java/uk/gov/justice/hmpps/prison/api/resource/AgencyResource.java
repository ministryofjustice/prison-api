package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.AgencyPrisonerPayProfile;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.LocationSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.core.ReferenceData;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.AgencyPrisonerPayProfileService;
import uk.gov.justice.hmpps.prison.service.AgencyService;

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
    // Added to the description of each endpoint that HMPPS Registers replaces
    private static final String REGISTERS_NOTE = """
        <p>The HMPPS Registers endpoints need the role HMPPS_REGISTERS_API__R. Agency data rarely changes, so please cache it.
        HMPPS Registers API documentation: https://agency-register-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html</p>
        """;

    private final AgencyService agencyService;
    private final AgencyPrisonerPayProfileService agencyPrisonerPayProfileService;

    public AgencyResource(
        final AgencyService agencyService,
        final AgencyPrisonerPayProfileService agencyPrisonerPayProfileService
    ) {
        this.agencyService = agencyService;
        this.agencyPrisonerPayProfileService = agencyPrisonerPayProfileService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of active agencies.", description = """
        <p>List of active agencies.</p>
        <p>DEPRECATED. Use GET https://agency-register.hmpps.service.justice.gov.uk/api/agencies in HMPPS Registers instead. Differences:</p>
        <ul>
        <li>All active agencies come back in one response. There is no paging, so the Page-Offset and Page-Limit headers are not used.</li>
        <li>agencyType is a name such as PRISON or COURT, not the NOMIS code such as INST or CRT.</li>
        <li>Prison descriptions are the register's prison names, which deliberately differ from the NOMIS descriptions.</li>
        </ul>
        """ + REGISTERS_NOTE, deprecated = true)
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping
    @Deprecated
    public ResponseEntity<List<Agency>> getAgencies(@RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of agency records.") final Long pageOffset,
                                                    @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of agency records returned.") final Long pageLimit) {
        return agencyService.getAgencies(pageOffset, pageLimit).getResponse();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of agencies by type", description = """
        <p>List of active agencies by type.</p>
        <p>DEPRECATED. Use GET https://agency-register.hmpps.service.justice.gov.uk/api/agencies/legacy-type/{type} in HMPPS Registers instead.
        It takes the same NOMIS type codes, such as INST, CRT and HOSPITAL. GET https://agency-register.hmpps.service.justice.gov.uk/api/agencies/type/{type}
        does the same using the register's type names, such as PRISON and COURT. Differences:</p>
        <ul>
        <li>activeOnly works the same way and defaults to true.</li>
        <li>There are no courtType, jurisdictionCode, areaCode, regionCode or establishmentType filters. Filter the response on courtType, area.code or region.code instead.</li>
        <li>The register does not hold the NOMIS types TRN, PAR or PNP.</li>
        <li>Results are sorted by agencyId, not by description.</li>
        <li>Each agency always includes its addresses, phones, emails, area and region, except that prisons have no phones, emails, area or region here.
        For those, use GET https://agency-register.hmpps.service.justice.gov.uk/prisons/id/{prisonId}/details.</li>
        <li>agencyType is a name such as PRISON or COURT, not the NOMIS code, and deactivationDate is called inactiveDate.
        Address, phone and email fields differ as described on GET /api/agencies/{agencyId}.</li>
        </ul>
        """ + REGISTERS_NOTE, deprecated = true)
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/type/{type}")
    @Deprecated
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
    @Operation(summary = "Agency detail.", description = """
        <p>Agency detail.</p>
        <p>DEPRECATED. Use GET https://agency-register.hmpps.service.justice.gov.uk/api/agencies/{agencyId} in HMPPS Registers instead. Differences:</p>
        <ul>
        <li>It returns the agency whether it is active or not. There are no activeOnly, agencyType, withAddresses, skipFormatLocation or withAreas parameters.</li>
        <li>Addresses, phones, emails, area and region are always included, except that a prison has no phones, emails, area or region here.
        For those, use GET https://agency-register.hmpps.service.justice.gov.uk/prisons/id/{prisonId}/details.</li>
        <li>agencyType is a name such as PRISON or COURT, not the NOMIS code such as INST or CRT, and deactivationDate is called inactiveDate.</li>
        <li>Addresses have addressLine1, addressLine2, town, county, postcode and country. Phones have a number but no type or extension.
        Emails have an address field.</li>
        <li>For a prison, description is the register's prison name, which deliberately differs from NOMIS, and longDescription is the NOMIS description.</li>
        </ul>
        """ + REGISTERS_NOTE, deprecated = true)
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/{agencyId}")
    @Deprecated
    public Agency getAgency(
        @PathVariable @Parameter(description = "The ID of the agency", required = true) final String agencyId,
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
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of receptions with capacity for agency.", description = "List of active receptions with capacity for agency.")
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/{agencyId}/receptionsWithCapacity")
    @SlowReportQuery
    public List<OffenderCell> getAgencyActiveReceptionsWithCapacity(
        @PathVariable @Parameter(required = true) final String agencyId,
        @RequestParam(value = "attribute", required = false) @Parameter(description = "Restricts list of receptions returned to those that have a specified attribute.") final String attribute
    ) {
        return agencyService.getReceptionsWithCapacityInAgency(agencyId, attribute);
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
        @PathVariable @Parameter(required = true) final String agencyId,
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
        @PathVariable @Parameter(required = true)
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
        In all the main usages of the endpoint we found that the clients didn't need or use the contact details so have deprecated the endpoint.</p>
        <p>Use GET https://agency-register.hmpps.service.justice.gov.uk/api/agencies/legacy-type/INST in HMPPS Registers to get the list of active prisons,
        or GET https://agency-register.hmpps.service.justice.gov.uk/prisons/id/{prisonId}/details for one prison's addresses and department contact details.</p>
        """ + REGISTERS_NOTE, deprecated = true)
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
        <p>DEPRECATED. Use GET https://agency-register.hmpps.service.justice.gov.uk/api/agencies/legacy-type/INST in HMPPS Registers instead. Differences:</p>
        <ul>
        <li>Prisons are sorted by agencyId, not by description.</li>
        <li>description is the register's prison name, which deliberately differs from NOMIS, and longDescription is the NOMIS description.</li>
        <li>agencyType is PRISON, not INST.</li>
        <li>Each prison also includes its addresses and inactiveDate.</li>
        </ul>
        """ + REGISTERS_NOTE, deprecated = true)
    @ReferenceData(description = "Agency data is considered non-sensitive")
    @GetMapping("/prisons")
    @SlowReportQuery
    @Deprecated
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
    public PrisonContactDetail getPrisonContactDetail(@PathVariable @Parameter(required = true) final String agencyId) {
        return agencyService.getPrisonContactDetail(agencyId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return the payment profile data for the given Agency.", description = "Each agency can configure its own pay profile and this endpoint provides its key data, such as min/max pay and bonus rates. Requires VIEW_PRISON_DATA.")
    @PreAuthorize("hasRole('VIEW_PRISON_DATA')")
    @GetMapping("/{agencyId}/pay-profile")
    public AgencyPrisonerPayProfile getAgencyPayProfile(@PathVariable @Parameter(required = true) final String agencyId) {
        return agencyPrisonerPayProfileService.getAgencyPrisonerPayProfile(agencyId);
    }
}
