package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.model.ScheduledAppointmentDto;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.jpa.model.ScheduledAppointment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/schedules"})
@Validated
@SuppressWarnings("unused")
public interface ScheduleResource {

    @PostMapping("/{agencyId}/events-by-location-ids")
    @ApiOperation(value = "Get all events for given date for prisoners in listed cells. Note secondary sort is by start time", notes = "Get all events for given date for prisoners in listed cells. Note secondary sort is by start time", nickname = "getGroupEvents")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerSchedule.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PrisonerSchedule> getEventsByLocationId(@ApiParam(value = "The prison.", required = true) @PathVariable("agencyId") String agencyId,
                                                 @ApiParam(value = "The required location ids", required = true) @RequestBody @NotEmpty List<Long> body,
                                                 @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  @RequestParam(value = "date", required = false) LocalDate date,
                                                 @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot,
                                                 @ApiParam(value = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                 @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{agencyId}/locations/{locationId}/usage/{usage}")
    @ApiOperation(value = "Get all Prisoner events for given date at location.", notes = "Get all Prisoner events for given date at location.", nickname = "getLocationEvents")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerSchedule.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PrisonerSchedule> getLocationEvents(@ApiParam(value = "The prison.", required = true) @PathVariable("agencyId") String agencyId,
                                             @ApiParam(value = "The location id where event is held.", required = true) @PathVariable("locationId") Long locationId,
                                             @ApiParam(value = "The locationUsage code from the location object - one of the INTERNAL_LOCATION_USAGE reference codes.", required = true) @PathVariable("usage") String usage,
                                             @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date", required = false) LocalDate date,
                                             @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot,
                                             @ApiParam(value = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                             @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/locations/{locationId}/activities")

    @ApiOperation(value = "Get all Prisoner activities for given date at location.", notes = "Get all Prisoner activities for given date at location.", nickname = "getActivitiesAtLocation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerSchedule.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PrisonerSchedule> getActivitiesAtLocation(
            @ApiParam(value = "The location id where activity is held.", required = true) @PathVariable("locationId") Long locationId,
            @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date", required = false) LocalDate date,
            @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot,
            @ApiParam(value = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
            @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder,
            @ApiParam(value = "Include suspended scheduled activity - defaults to false") @RequestParam(value = "includeSuspended", required = false) boolean includeSuspended);

    @GetMapping("/{agencyId}/activities")

    @ApiOperation(value = "Get all Prisoner activities for given date.", notes = "Get all Prisoner activities for given date", nickname = "getActivitiesAtAllLocations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerSchedule.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PrisonerSchedule> getActivitiesAtAllLocations(@ApiParam(value = "The prison.", required = true) @PathVariable("agencyId") String agencyId,
                                                       @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date", required = false) LocalDate date,
                                                       @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot,
                                                       @ApiParam(value = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                       @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{agencyId}/activities-by-date-range")

    @ApiOperation(value = "Get all Prisoner activities for given date.", notes = "Get all Prisoner activities for given date range", nickname = "getActivitiesAtAllLocationsByDateRange")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerSchedule.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PrisonerSchedule> getActivitiesAtAllLocationsByDateRange(@ApiParam(value = "The prison.", required = true) @PathVariable("agencyId") String agencyId,
                                                                  @ApiParam(value = "From date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                                  @ApiParam(value = "To Date of whereabouts list, default from date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                                  @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot,
                                                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @PostMapping("/{agencyId}/activities")
    @ApiOperation(value = "", nickname = "getActivities")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List")})
    List<PrisonerSchedule> getActivities(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                         @ApiParam(value = "The required offender numbers (mandatory)", required = true) @RequestBody List<String> body,
                                         @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date", required = false) LocalDate date,
                                         @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot,
                                         @ApiParam(value = "Whether to include 'excluded' activities in the results", defaultValue = "false") @RequestParam(value = "includeExcluded", required = false, defaultValue = "false") boolean includeExcluded);

    @PostMapping("/{agencyId}/appointments")
    @ApiOperation(value = "", nickname = "getAppointmentsForOffenders")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List")})
    List<PrisonerSchedule> getAppointmentsForOffenders(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                           @ApiParam(value = "The required offender numbers (mandatory)", required = true) @RequestBody List<String> body,
                                           @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date", required = false) LocalDate date,
                                           @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot);


    @GetMapping("/{agencyId}/appointments")
    @ApiOperation(value = "", nickname = "getAppointments")
    @ApiResponses(value = {
    @ApiResponse(code = 200, message = "", response = ScheduledAppointment.class, responseContainer = "List")})
    List<ScheduledAppointmentDto> getAppointments(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                                  @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date") LocalDate date,
                                                  @ApiParam(value = "Location id") @RequestParam(value = "locationId", required = false) Long locationId,
                                                  @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot);

    @PostMapping("/{agencyId}/courtEvents")
    @ApiOperation(value = "", nickname = "getCourtEvents")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List")})
    List<PrisonerSchedule> getCourtEvents(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                          @ApiParam(value = "The required offender numbers (mandatory)", required = true) @RequestBody List<String> body,
                                          @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date", required = false) LocalDate date,
                                          @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot);

    @PostMapping("/{agencyId}/externalTransfers")
    @ApiOperation(value = "", nickname = "getExternalTransfers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List")})
    List<PrisonerSchedule> getExternalTransfers(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                                @ApiParam(value = "The required offender numbers (mandatory)", required = true) @RequestBody List<String> body,
                                                @ApiParam(value = "Date of scheduled transfer") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("date") LocalDate date);

    @PostMapping("/{agencyId}/visits")
    @ApiOperation(value = "", nickname = "getVisits")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List")})
    List<PrisonerSchedule> getVisits(@ApiParam(value = "", required = true) @PathVariable("agencyId") String agencyId,
                                     @ApiParam(value = "The required offender numbers (mandatory)", required = true) @RequestBody List<String> body,
                                     @ApiParam(value = "Date of whereabouts list, default today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date", required = false) LocalDate date,
                                     @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") @RequestParam(value = "timeSlot", required = false) TimeSlot timeSlot);


}
