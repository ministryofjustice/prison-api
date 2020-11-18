package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.api.model.ScheduledAppointmentDto;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;
import uk.gov.justice.hmpps.prison.service.SchedulesService;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;


/**
 * Implementation of /schedules endpoint.
 */
@RestController
@Api(tags = {"schedules"})
@Validated
@RequestMapping("${api.base.path}/schedules")
public class ScheduleResource {
    private final SchedulesService schedulesService;
    private final AppointmentsService appointmentsService;

    public ScheduleResource(final SchedulesService schedulesService, final AppointmentsService appointmentsService) {
        this.schedulesService = schedulesService;
        this.appointmentsService = appointmentsService;
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get all events for given date for prisoners in listed cells. Note secondary sort is by start time", notes = "Get all events for given date for prisoners in listed cells. Note secondary sort is by start time", nickname = "getGroupEvents")
    @PostMapping("/{agencyId}/events-by-location-ids")
    public List<PrisonerSchedule> getEventsByLocationId(@PathVariable("agencyId") @ApiParam(value = "The prison.", required = true) final String agencyId, @NotEmpty @RequestBody @ApiParam(value = "The required location ids", required = true) final List<Long> locationIds,
                                                        @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {

        return schedulesService.getLocationGroupEventsByLocationId(agencyId, locationIds,
                date, timeSlot, sortFields, sortOrder);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get all Prisoner events for given date at location.", notes = "Get all Prisoner events for given date at location.", nickname = "getLocationEvents")
    @GetMapping("/{agencyId}/locations/{locationId}/usage/{usage}")
    public List<PrisonerSchedule> getLocationEvents(@PathVariable("agencyId") @ApiParam(value = "The prison.", required = true) final String agencyId, @PathVariable("locationId") @ApiParam(value = "The location id where event is held.", required = true) final Long locationId, @PathVariable("usage") @ApiParam(value = "The locationUsage code from the location object - one of the INTERNAL_LOCATION_USAGE reference codes.", required = true) final String usage,
                                                    @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields,
                                                    @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {

        return schedulesService.getLocationEvents(agencyId, locationId, usage, date, timeSlot, sortFields, sortOrder);

    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get all Prisoner activities for given date at location.", notes = "Get all Prisoner activities for given date at location.", nickname = "getActivitiesAtLocation")
    @GetMapping("/locations/{locationId}/activities")
    public List<PrisonerSchedule> getActivitiesAtLocation(@PathVariable("locationId") @ApiParam(value = "The location id where activity is held.", required = true) final Long locationId, @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder, @RequestParam(value = "includeSuspended", required = false) @ApiParam("Include suspended scheduled activity - defaults to false") final boolean includeSuspended) {
        return schedulesService.getActivitiesAtLocation(locationId, date, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get all Prisoner activities for given date.", notes = "Get all Prisoner activities for given date", nickname = "getActivitiesAtAllLocations")
    @GetMapping("/{agencyId}/activities")
    public List<PrisonerSchedule> getActivitiesAtAllLocations(@PathVariable("agencyId") @ApiParam(value = "The prison.", required = true) final String agencyId, @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder, @RequestParam(value = "includeSuspended", required = false) @ApiParam("Include suspended scheduled activity - defaults to false") final boolean includeSuspended) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, date, null, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get all Prisoner activities for given date.", notes = "Get all Prisoner activities for given date range", nickname = "getActivitiesAtAllLocationsByDateRange")
    @GetMapping("/{agencyId}/activities-by-date-range")
    public List<PrisonerSchedule> getActivitiesAtAllLocationsByDateRange(@PathVariable("agencyId") @ApiParam(value = "The prison.", required = true) final String agencyId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("From date of whereabouts list, default today") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("To Date of whereabouts list, default from date") final LocalDate toDate, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder, @RequestParam(value = "includeSuspended", required = false) @ApiParam("Include suspended scheduled activity - defaults to false") final boolean includeSuspended) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, fromDate, toDate, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @ApiOperation(value = "", nickname = "getAppointmentsForOffenders")
    @PostMapping("/{agencyId}/appointments")
    public List<PrisonerSchedule> getAppointmentsForOffenders(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot) {
        return schedulesService.getAppointments(agencyId, body, date, timeSlot);

    }

    @ApiOperation(value = "", nickname = "getAppointments")
    @GetMapping("/{agencyId}/appointments")
    public List<ScheduledAppointmentDto> getAppointments(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestParam("date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Date the appointments are scheduled", required = true) final LocalDate date, @RequestParam(value = "locationId", required = false) @ApiParam("Location id") final Long locationId, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot) {
        return appointmentsService.getAppointments(agencyId, date, locationId, timeSlot);
    }

    @PostMapping("/{agencyId}/visits")
    public List<PrisonerSchedule> getVisits(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot) {
        return schedulesService.getVisits(agencyId, body, date, timeSlot);
    }

    @ApiOperation(value = "", nickname = "getActivitiesForBookings")
    @PostMapping("/{agencyId}/activities")
    public List<PrisonerSchedule> getActivitiesForBookings(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot, @RequestParam(value = "includeExcluded", required = false, defaultValue = "false") @ApiParam(value = "Whether to include 'excluded' activities in the results", defaultValue = "false") final boolean includeExcluded) {
        return schedulesService.getActivitiesByEventIds(agencyId, body, date, timeSlot, includeExcluded);
    }

    @ApiOperation(value = "", nickname = "getActivitiesByEventIds")
    @PostMapping("/{agencyId}/activities-by-event-ids")
    public List<PrisonerSchedule> getActivitiesByEventIds(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @NotEmpty @RequestBody @ApiParam(value = "Event ids(mandatory)", required = true) final List<Long> eventIds) {
        return schedulesService.getActivitiesByEventIds(eventIds);
    }

    @ApiOperation(value = "", nickname = "getCourtEvents")
    @PostMapping("/{agencyId}/courtEvents")
    public List<PrisonerSchedule> getCourtEvents(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @ApiParam(value = "AM, PM or ED", allowableValues = "AM,PM,ED") final TimeSlot timeSlot) {
        return schedulesService.getCourtEvents(agencyId, body, date, timeSlot);
    }

    @ApiOperation(value = "", nickname = "getExternalTransfers")
    @PostMapping("/{agencyId}/externalTransfers")
    public List<PrisonerSchedule> getExternalTransfers(@PathVariable("agencyId") @ApiParam(value = "", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam("date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Date of scheduled transfer") final LocalDate date) {
        return schedulesService.getExternalTransfers(agencyId, body, date);
    }
}
