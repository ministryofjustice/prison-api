package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerActivitiesCount;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;
import uk.gov.justice.hmpps.prison.service.SchedulesService;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of /schedules endpoint.
 */
@RestController
@Tag(name = "schedules")
@Validated
@RequestMapping(value = "${api.base.path}/schedules", produces = "application/json")
public class ScheduleResource {
    private final SchedulesService schedulesService;
    private final AppointmentsService appointmentsService;

    public ScheduleResource(final SchedulesService schedulesService, final AppointmentsService appointmentsService) {
        this.schedulesService = schedulesService;
        this.appointmentsService = appointmentsService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get all events for given date for prisoners in listed cells. Note secondary sort is by start time", description = "Get all events for given date for prisoners in listed cells. Note secondary sort is by start time")
    @PostMapping("/{agencyId}/events-by-location-ids")
    public List<PrisonerSchedule> getEventsByLocationId(@PathVariable("agencyId") @Parameter(description = "The prison.", required = true) final String agencyId, @NotEmpty @RequestBody @Parameter(description = "The required location ids", required = true) final List<Long> locationIds,
                                                        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {

        return schedulesService.getLocationGroupEventsByLocationId(agencyId, locationIds,
                date, timeSlot, sortFields, sortOrder);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get all Prisoner events for given date at location.", description = "Get all Prisoner events for given date at location.")
    @GetMapping("/{agencyId}/locations/{locationId}/usage/{usage}")
    public List<PrisonerSchedule> getLocationEvents(@PathVariable("agencyId") @Parameter(description = "The prison.", required = true) final String agencyId, @PathVariable("locationId") @Parameter(description = "The location id where event is held.", required = true) final Long locationId, @PathVariable("usage") @Parameter(description = "The locationUsage code from the location object - one of the INTERNAL_LOCATION_USAGE reference codes.", required = true) final String usage,
                                                    @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields,
                                                    @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {

        return schedulesService.getLocationEvents(agencyId, locationId, usage, date, timeSlot, sortFields, sortOrder);

    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get all Prisoner activities for given date at location.", description = "Get all Prisoner activities for given date at location.")
    @GetMapping("/locations/{locationId}/activities")
    public List<PrisonerSchedule> getActivitiesAtLocation(@PathVariable("locationId") @Parameter(description = "The location id where activity is held.", required = true) final Long locationId, @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder, @RequestParam(value = "includeSuspended", required = false) @Parameter(description = "Include suspended scheduled activity - defaults to false") final boolean includeSuspended) {
        return schedulesService.getActivitiesAtLocation(locationId, date, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get all Prisoner activities for given date.", description = "Get all Prisoner activities for given date")
    @GetMapping("/{agencyId}/activities")
    public List<PrisonerSchedule> getActivitiesAtAllLocations(@PathVariable("agencyId") @Parameter(description = "The prison.", required = true) final String agencyId, @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder, @RequestParam(value = "includeSuspended", required = false) @Parameter(description = "Include suspended scheduled activity - defaults to false") final boolean includeSuspended) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, date, null, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get all Prisoner activities for given date.", description = "Get all Prisoner activities for given date range")
    @GetMapping("/{agencyId}/activities-by-date-range")
    public List<PrisonerSchedule> getActivitiesAtAllLocationsByDateRange(@PathVariable("agencyId") @Parameter(description = "The prison.", required = true) final String agencyId,
                                                                         @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "From date of whereabouts list, default today") final LocalDate fromDate,
                                                                         @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "To Date of whereabouts list, default from date") final LocalDate toDate,
                                                                         @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot,
                                                                         @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") final String sortFields,
                                                                         @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder,
                                                                         @RequestParam(value = "includeSuspended", required = false) @Parameter(description = "Include suspended scheduled activity - defaults to false") final boolean includeSuspended) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, fromDate, toDate, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get all Prisoner activities for given date.", description = "Get all Prisoner activities for given date range")
    @GetMapping("/{agencyId}/suspended-activities-by-date-range")
    public List<PrisonerSchedule> getSuspendedActivitiesAtAllLocationsByDateRange(@PathVariable("agencyId") @Parameter(description = "The prison.", required = true) final String agencyId,
                                                                         @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "From date of whereabouts list, default today") final LocalDate fromDate,
                                                                         @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "To Date of whereabouts list, default from date") final LocalDate toDate,
                                                                         @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot) {
        return schedulesService.getSuspendedActivitiesAtAllLocations(agencyId, fromDate, toDate, timeSlot);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get all Prisoner activities for given date.", description = "Get count of suspended prisoner activities for given date range")
    @PostMapping("/{agencyId}/count-activities")
    public PrisonerActivitiesCount getCountActivitiesByDateRange(@PathVariable("agencyId") @Parameter(description = "The prison.", required = true, example = "MDI") final String agencyId,
                                                                 @RequestParam(value = "fromDate") @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "From date of whereabouts list") @NotNull final LocalDate fromDate,
                                                                 @RequestParam(value = "toDate") @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "To Date of whereabouts list") @NotNull final LocalDate toDate,
                                                                 @RequestParam(value = "timeSlots") @Parameter(description = "Set of one or more of AM, PM or ED") @NotEmpty final Set<TimeSlot> timeSlots,
                                                                 @RequestBody @Parameter(description = "Map of booking IDs to their occurrence counts") final Map<Long, Long> attendanceCounts) {
        return schedulesService.getCountActivities(agencyId, fromDate, toDate, timeSlots, attendanceCounts);
    }

    @Operation
    @PostMapping("/{agencyId}/appointments")
    public List<PrisonerSchedule> getAppointmentsForOffenders(@PathVariable("agencyId") @Parameter(required = true) final String agencyId, @RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot) {
        return schedulesService.getAppointments(agencyId, body, date, timeSlot);

    }

    @Operation
    @GetMapping("/{agencyId}/appointments")
    public List<ScheduledAppointmentDto> getAppointments(@PathVariable("agencyId") @Parameter(required = true) final String agencyId, @RequestParam("date") @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date the appointments are scheduled", required = true) final LocalDate date, @RequestParam(value = "locationId", required = false) @Parameter(description = "Location id") final Long locationId, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot) {
        return appointmentsService.getAppointments(agencyId, date, locationId, timeSlot);
    }

    @PostMapping("/{agencyId}/visits")
    public List<PrisonerSchedule> getVisits(@PathVariable("agencyId") @Parameter(required = true) final String agencyId, @RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot) {
        return schedulesService.getVisits(agencyId, body, date, timeSlot);
    }

    @Operation
    @PostMapping("/{agencyId}/activities")
    public List<PrisonerSchedule> getActivitiesForBookings(@PathVariable("agencyId") @Parameter(required = true) final String agencyId, @RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot, @RequestParam(value = "includeExcluded", required = false, defaultValue = "false") @Parameter(description = "Whether to include 'excluded' activities in the results") final boolean includeExcluded) {
        return schedulesService.getActivitiesByEventIds(agencyId, body, date, timeSlot, includeExcluded);
    }

    @Operation
    @PostMapping("/{agencyId}/activities-by-event-ids")
    public List<PrisonerSchedule> getActivitiesByEventIds(@PathVariable("agencyId") @Parameter(required = true) final String agencyId, @NotEmpty @RequestBody @Parameter(description = "Event ids(mandatory)", required = true) final List<Long> eventIds) {
        return schedulesService.getActivitiesByEventIds(agencyId, eventIds);
    }

    @Operation
    @PostMapping("/{agencyId}/courtEvents")
    public List<PrisonerSchedule> getCourtEvents(@PathVariable("agencyId") @Parameter(required = true) final String agencyId, @RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of whereabouts list, default today") final LocalDate date, @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot) {
        return schedulesService.getCourtEvents(agencyId, body, date, timeSlot);
    }

    @Operation
    @PostMapping("/{agencyId}/externalTransfers")
    public List<PrisonerSchedule> getExternalTransfers(@PathVariable("agencyId") @Parameter(required = true) final String agencyId, @RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> body, @RequestParam("date") @DateTimeFormat(iso = ISO.DATE) @Parameter(description = "Date of scheduled transfer") final LocalDate date) {
        return schedulesService.getExternalTransfers(agencyId, body, date);
    }
}
