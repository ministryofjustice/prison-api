package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.service.OffenderIepReview;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/agencies"})
@SuppressWarnings("unused")
public interface AgencyResource {

    @GetMapping
    @ApiOperation(value = "List of active agencies.", notes = "List of active agencies.", nickname = "getAgencies")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Agency.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<Agency>> getAgencies(@ApiParam(value = "Requested offset of first record in returned collection of agency records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                    @ApiParam(value = "Requested limit to number of agency records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit);

    @GetMapping("/type/{type}")
    @ApiOperation(value = "List of agencies by type", notes = "List of active agencies by type")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Agency.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Agency> getAgenciesByType(@ApiParam(value = "Agency Type") @PathVariable(value = "type") final String type,
                                   @ApiParam(value = "Only return active agencies") @RequestParam(value = "activeOnly", defaultValue = "true", required = false) boolean activeOnly);

    @GetMapping("/{agencyId}")
    @ApiOperation(value = "Agency detail.", notes = "Agency detail.", nickname = "getAgency")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Agency.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Agency getAgency(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                @ApiParam(value = "Only return active agencies", defaultValue = "true") @RequestParam(value = "activeOnly", defaultValue = "true", required = false)  boolean activeOnly,
                                @ApiParam(value = "Agency Type") @RequestParam(value = "agencyType", required = false) String agencyType);

    @GetMapping("/{agencyId}/eventLocations")
    @ApiOperation(value = "List of locations for agency where events (appointments, visits, activities) could be held.", notes = "List of locations for agency where events (appointments, visits, activities) could be held.", nickname = "getAgencyEventLocations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Location> getAgencyEventLocations(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                                            @ApiParam(value = "Comma separated list of one or more of the following fields - <b>description, userDescription</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                            @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{agencyId}/eventLocationsBooked")
    @ApiOperation(value = "List of locations for agency where events (appointments, visits, activities) are being held.", notes = "List of locations for agency where events (appointments, visits, activities) are being held.", nickname = "getAgencyEventLocationsBooked")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Location> getAgencyEventLocationsBooked(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                                                        @ApiParam(value = "Filter list to only return locations which prisoners will be attending on this day", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("bookedOnDay") LocalDate bookedOnDay,
                                                                        @ApiParam(value = "Only return locations which prisoners will be attending in this time slot (AM, PM or ED, and bookedOnDay must be specified)", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot);

    @GetMapping("/{agencyId}/locations")
    @ApiOperation(value = "List of active internal locations for agency.", notes = "List of active internal locations for agency.", nickname = "getAgencyLocations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Location> getAgencyLocations(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                                  @ApiParam(value = "Restricts list of locations returned to those that can be used for the specified event type.") @RequestParam(value = "eventType", required = false) String eventType,
                                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>description, userDescription</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{agencyId}/locations/type/{type}")
    @ApiOperation(value = "List of active internal locations for agency by type.", notes = "List of active internal locations for agency by type.", nickname = "getAgencyLocationsByType")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Location> getAgencyLocationsByType(@ApiParam(value = "The prison", required = true) @PathVariable("agencyId") String agencyId,
                                                  @ApiParam(value = "Restricts list of locations returned to those of the passed type.", required = true) @PathVariable("type") String type);

    @GetMapping("/{agencyId}/iepLevels")
    @ApiOperation(value = "List of active IEP levels for agency.", notes = "List of active IEP levels for agency.", nickname = "getAgencyIepLevels")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = IepLevel.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<IepLevel> getAgencyIepLevels(@ApiParam(value = "agencyId", required = true) @PathVariable("agencyId") String agencyId);

    @GetMapping("/{agencyId}/locations/groups")
    @ApiOperation(value = "List of all available Location Groups at agency.", notes = "List of all available Location Groups at agency.", nickname = "getAvailableLocationGroups")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LocationGroup.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<LocationGroup> getAvailableLocationGroups(@ApiParam(value = "The prison", required = true) @PathVariable("agencyId") String agencyId);

    @GetMapping("/caseload/{caseload}")
    @ApiOperation(value = "List of agencies for caseload.", notes = "List of agencies for caseload.", nickname = "getAgenciesByCaseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Agency.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Agency> getAgenciesByCaseload(@ApiParam(value = "", required = true) @PathVariable("caseload") String caseload);

    @GetMapping("/prison")
    @ApiOperation(value = "List of prison contact details.", notes = "List of prison contact details.", nickname = "getPrisonContactDetailList")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonContactDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PrisonContactDetail> getPrisonContactDetailList();

    @GetMapping("/prison/{agencyId}")
    @ApiOperation(value = "Prison contact detail.", notes = "Prison contact detail.", nickname = "getPrisonContactDetail")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonContactDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    PrisonContactDetail getPrisonContactDetail(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId);

    @GetMapping("/{agencyId}/iepReview")
    @ApiOperation(value = "Per offender information necessary for IEP review.", notes = "IEP review information", nickname = "getPrisonIepReview")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderIepReview.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<List<OffenderIepReview>> getPrisonIepReview(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                                               @ApiParam(value = "IEP level to filter by.") @RequestParam("iepLevel") String iepLevel,
                                                               @ApiParam(value = "Offender location to filter by.") @RequestParam("location") String location,
                                                               @ApiParam(value = "Requested offset of first record in returned offenders.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                               @ApiParam(value = "Requested limit to number of offenders returned.", defaultValue = "20") @RequestHeader(value = "Page-Limit", defaultValue = "20") Long pageLimit);

}
