package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;

import javax.ws.rs.*;
import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/schedules"})
@SuppressWarnings("unused")
public interface ScheduleResource {

    @GET
    @Path("/{agencyId}/groups/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all events for given date for prisoners in listed cells. Note secondary sort is by start time", notes = "Get all events for given date for prisoners in listed cells. Note secondary sort is by start time", nickname="getGroupEvents")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PrisonerSchedule.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    List<PrisonerSchedule> getGroupEvents(@ApiParam(value = "The prison.", required = true) @PathParam("agencyId") String agencyId,
                                          @ApiParam(value = "The location list name.", required = true) @PathParam("name") String name,
                                          @ApiParam(value = "Date of whereabouts list, default today") @QueryParam("date") LocalDate date,
                                          @ApiParam(value = "AM, PM or ED") @QueryParam("timeSlot") TimeSlot timeSlot,
                                          @ApiParam(value = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") @HeaderParam("Sort-Fields") String sortFields,
                                          @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{agencyId}/locations/{locationId}/usage/{usage}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Prisoner events for given date at location.", notes = "Get all Prisoner events for given date at location.", nickname="getLocationEvents")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PrisonerSchedule.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    List<PrisonerSchedule> getLocationEvents(@ApiParam(value = "The prison.", required = true) @PathParam("agencyId") String agencyId,
                                                @ApiParam(value = "The location id where event is held.", required = true) @PathParam("locationId") Long locationId,
                                                @ApiParam(value = "The locationUsage code from the location object - one of the INTERNAL_LOCATION_USAGE reference codes.", required = true) @PathParam("usage") String usage,
                                                @ApiParam(value = "Date of whereabouts list, default today") @QueryParam("date") LocalDate date,
                                                @ApiParam(value = "AM, PM or ED") @QueryParam("timeSlot") TimeSlot timeSlot,
                                                @ApiParam(value = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{agencyId}/activities")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Prisoner activities for given date.", notes = "Get all Prisoner activities for given date", nickname="getActivitiesAtAllLocations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerSchedule.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    List<PrisonerSchedule> getActivitiesAtAllLocations(@ApiParam(value = "The prison.", required = true) @PathParam("agencyId") String agencyId,
                                                                 @ApiParam(value = "Date of whereabouts list, default today") @QueryParam("date") LocalDate date,
                                                                 @ApiParam(value = "AM, PM or ED") @QueryParam("timeSlot") TimeSlot timeSlot,
                                                                 @ApiParam(value = "Comma separated list of one or more of the following fields - <b>cellLocation or lastName</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                 @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @POST
    @Path("/{agencyId}/activities")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", nickname="getActivities")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List") })
    List<PrisonerSchedule> getActivities(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                        @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                        @ApiParam(value = "Date of whereabouts list, default today") @QueryParam("date") LocalDate date,
                                        @ApiParam(value = "AM, PM or ED") @QueryParam("timeSlot") TimeSlot timeSlot,
                                        @ApiParam(value = "Whether to include 'excluded' activities in the results", defaultValue = "false") @QueryParam("includeExcluded") boolean includeExcluded);

    @POST
    @Path("/{agencyId}/appointments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", nickname="getAppointments")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List") })
    List<PrisonerSchedule> getAppointments(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                            @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                            @ApiParam(value = "Date of whereabouts list, default today") @QueryParam("date") LocalDate date,
                                            @ApiParam(value = "AM, PM or ED") @QueryParam("timeSlot") TimeSlot timeSlot);

    @POST
    @Path("/{agencyId}/courtEvents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", nickname="getCourtEvents")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List") })
    List<PrisonerSchedule> getCourtEvents(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                          @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                          @ApiParam(value = "Date of whereabouts list, default today") @QueryParam("date") LocalDate date,
                                          @ApiParam(value = "AM, PM or ED") @QueryParam("timeSlot") TimeSlot timeSlot);

    @POST
    @Path("/{agencyId}/externalTransfers")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", nickname="getExternalTransfers")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List") })
    List<PrisonerSchedule> getExternalTransfers(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                                      @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                                      @ApiParam(value = "Date of scheduled transfer") @QueryParam("date") LocalDate date);

    @POST
    @Path("/{agencyId}/visits")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", nickname="getVisits")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List") })
    List<PrisonerSchedule> getVisits(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                @ApiParam(value = "Date of whereabouts list, default today") @QueryParam("date") LocalDate date,
                                @ApiParam(value = "AM, PM or ED") @QueryParam("timeSlot") TimeSlot timeSlot);


}
