package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentTypes;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.IepLevel;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.LocationGroup;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.service.AgencyService;
import uk.gov.justice.hmpps.prison.service.LocationGroupService;
import uk.gov.justice.hmpps.prison.service.OffenderIepReview;
import uk.gov.justice.hmpps.prison.service.OffenderIepReviewSearchCriteria;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ACTIVE_ONLY;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

@RestController
@Validated
@Api(tags = {"agencies"})
@RequestMapping("${api.base.path}/agencies")
public class AgencyResource {
    private final AgencyService agencyService;
    private final LocationGroupService locationGroupService;

    public AgencyResource(
            final AgencyService agencyService,
            final LocationGroupService locationGroupService) {
        this.agencyService = agencyService;
        this.locationGroupService = locationGroupService;
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of active agencies.", notes = "List of active agencies.", nickname = "getAgencies")
    @GetMapping
    public ResponseEntity<List<Agency>> getAgencies(@RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of agency records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of agency records returned.", defaultValue = "10") final Long pageLimit) {
        return agencyService.getAgencies(pageOffset, pageLimit).getResponse();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of agencies by type", notes = "List of active agencies by type")
    @GetMapping("/type/{type}")
    public List<Agency> getAgenciesByType(@PathVariable("type") @ApiParam(value = "Agency Type", required = true) final String agencyType, @RequestParam(value = "activeOnly", defaultValue = "true", required = false) @ApiParam("Only return active agencies") boolean activeOnly) {
        return agencyService.getAgenciesByType(agencyType, activeOnly);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Agency detail.", notes = "Agency detail.", nickname = "getAgency")
    @GetMapping("/{agencyId}")
    public Agency getAgency(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestParam(value = "activeOnly", defaultValue = "true", required = false) @ApiParam(value = "Only return active agencies", defaultValue = "true") final boolean activeOnly, @RequestParam(value = "agencyType", required = false) @ApiParam("Agency Type") final String agencyType) {
        return agencyService.getAgency(agencyId, activeOnly ? ACTIVE_ONLY : ALL, agencyType);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of active internal locations for agency.", notes = "List of active internal locations for agency.", nickname = "getAgencyLocations")
    @GetMapping("/{agencyId}/locations")
    public List<Location> getAgencyLocations(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestParam(value = "eventType", required = false) @ApiParam("Restricts list of locations returned to those that can be used for the specified event type.") final String eventType, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>description, userDescription</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        return agencyService.getAgencyLocations(agencyId, eventType, sortFields, sortOrder);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of active cells with capacity for agency.", notes = "List of active cells with capacity for agency.", nickname = "getAgencyActiveCellsWithCapacity")
    @GetMapping("/{agencyId}/cellsWithCapacity")
    public List<OffenderCell> getAgencyActiveCellsWithCapacity(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestParam(value = "attribute", required = false) @ApiParam("Restricts list of cells returned to those that have a specified attribute.") final String attribute) {
        return agencyService.getCellsWithCapacityInAgency(agencyId, attribute);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of active internal locations for agency by type.", notes = "List of active internal locations for agency by type.", nickname = "getAgencyLocationsByType")
    @GetMapping("/{agencyId}/locations/type/{type}")
    public List<Location> getAgencyLocationsByType(@PathVariable("agencyId") @ApiParam(value = "The prison", required = true) final String agencyId, @PathVariable("type") @ApiParam(value = "Restricts list of locations returned to those of the passed type.", required = true) final String type) {
        return agencyService.getAgencyLocationsByType(agencyId, type);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of active IEP levels for agency.", notes = "List of active IEP levels for agency.", nickname = "getAgencyIepLevels")
    @GetMapping("/{agencyId}/iepLevels")
    public List<IepLevel> getAgencyIepLevels(@PathVariable("agencyId") @ApiParam(value = "agencyId", required = true) final String agencyId) {
        return agencyService.getAgencyIepLevels(agencyId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of all available Location Groups at agency.", notes = "List of all available Location Groups at agency.", nickname = "getAvailableLocationGroups")
    @GetMapping("/{agencyId}/locations/groups")
    public List<LocationGroup> getAvailableLocationGroups(@PathVariable("agencyId") @ApiParam(value = "The prison", required = true) final String agencyId) {
        return locationGroupService.getLocationGroupsForAgency(agencyId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of locations for agency where events (appointments, visits, activities) could be held.", notes = "List of locations for agency where events (appointments, visits, activities) could be held.", nickname = "getAgencyEventLocations")
    @GetMapping("/{agencyId}/eventLocations")
    public List<Location> getAgencyEventLocations(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>description, userDescription</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        return agencyService.getAgencyEventLocations(agencyId, sortFields, sortOrder);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of locations for agency where events (appointments, visits, activities) are being held.", notes = "List of locations for agency where events (appointments, visits, activities) are being held.", nickname = "getAgencyEventLocationsBooked")
    @GetMapping("/{agencyId}/eventLocationsBooked")
    public List<Location> getAgencyEventLocationsBooked(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestParam("bookedOnDay") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Filter list to only return locations which prisoners will be attending on this day", required = true) final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "Only return locations which prisoners will be attending in this time slot (AM, PM or ED, and bookedOnDay must be specified)", allowableValues = "AM,PM,ED") final TimeSlot timeSlot) {
        return agencyService.getAgencyEventLocationsBooked(agencyId, date, timeSlot);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of agencies for caseload.", notes = "List of agencies for caseload.", nickname = "getAgenciesByCaseload")
    @GetMapping("/caseload/{caseload}")
    public List<Agency> getAgenciesByCaseload(@PathVariable("caseload") @ApiParam(value = "", required = true) final String caseload) {
        return agencyService.getAgenciesByCaseload(caseload);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = PrisonContactDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of prison contact details.", notes = "List of prison contact details.", nickname = "getPrisonContactDetailList")
    @GetMapping("/prison")
    public List<PrisonContactDetail> getPrisonContactDetailList() {
        return agencyService.getPrisonContactDetail();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Prison contact detail.", notes = "Prison contact detail.", nickname = "getPrisonContactDetail")
    @GetMapping("/prison/{agencyId}")
    public PrisonContactDetail getPrisonContactDetail(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId) {
        return agencyService.getPrisonContactDetail(agencyId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Per offender information necessary for IEP review.", notes = "IEP review information", nickname = "getPrisonIepReview")
    @GetMapping("/{agencyId}/iepReview")
    public ResponseEntity<List<OffenderIepReview>> getPrisonIepReview(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId,
                                                                      @RequestParam("iepLevel") @ApiParam("IEP level to filter by.") final String iepLevel,
                                                                      @RequestParam("location") @ApiParam("Offender location to filter by.") final String location,
                                                                      @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned offenders.", defaultValue = "0") final Long pageOffset,
                                                                      @RequestHeader(value = "Page-Limit", defaultValue = "20") @ApiParam(value = "Requested limit to number of offenders returned.", defaultValue = "20") final Long pageLimit) {
        final var criteria = OffenderIepReviewSearchCriteria.builder()
                .agencyId(agencyId)
                .iepLevel(iepLevel)
                .location(location)
                .pageRequest(new PageRequest(pageOffset, pageLimit))
                .build();

        final var prisonIepReview = agencyService.getPrisonIepReview(criteria);

        return ResponseEntity.ok()
                .headers(prisonIepReview.getPaginationHeaders())
                .body(prisonIepReview.getItems());
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Return the establishment types for the given Agency.", notes = "An agency can have one to many establishment types. For example a prison could be both a youth and adult establishment.", nickname = "getAgencyEstablishmentTypes")
    @GetMapping("/{agencyId}/establishment-types")
    public AgencyEstablishmentTypes getAgencyEstablishmentTypes(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId) {
        return agencyService.getEstablishmentTypes(agencyId);
    }
}
