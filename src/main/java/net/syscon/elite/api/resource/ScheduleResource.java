package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.ResponseDelegate;
import net.syscon.elite.api.support.TimeSlot;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    GetGroupEventsResponse getGroupEvents(@ApiParam(value = "The prison.", required = true) @PathParam("agencyId") String agencyId,
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
    GetLocationEventsResponse getLocationEvents(@ApiParam(value = "The prison.", required = true) @PathParam("agencyId") String agencyId,
                                                @ApiParam(value = "The location id where event is held.", required = true) @PathParam("locationId") Long locationId,
                                                @ApiParam(value = "The locationUsage code from the location object - one of the INTERNAL_LOCATION_USAGE reference codes.", required = true) @PathParam("usage") String usage,
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
    GetActivitiesResponse getActivities(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
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
    GetAppointmentsResponse getAppointments(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
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
    GetCourtEventsResponse getCourtEvents(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
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
    GetExternalTransfersResponse getExternalTransfers(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                                      @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                                      @ApiParam(value = "Date of scheduled transfer") @QueryParam("date") LocalDate date);

    @POST
    @Path("/{agencyId}/visits")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", nickname="getVisits")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = PrisonerSchedule.class, responseContainer = "List") })
    GetVisitsResponse getVisits(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                @ApiParam(value = "Date of whereabouts list, default today") @QueryParam("date") LocalDate date,
                                @ApiParam(value = "AM, PM or ED") @QueryParam("timeSlot") TimeSlot timeSlot);

    class GetGroupEventsResponse extends ResponseDelegate {

        private GetGroupEventsResponse(final Response response) {
            super(response);
        }

        private GetGroupEventsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetGroupEventsResponse respond200WithApplicationJson(final List<PrisonerSchedule> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetGroupEventsResponse(responseBuilder.build(), entity);
        }

        public static GetGroupEventsResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetGroupEventsResponse(responseBuilder.build(), entity);
        }

        public static GetGroupEventsResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetGroupEventsResponse(responseBuilder.build(), entity);
        }

        public static GetGroupEventsResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetGroupEventsResponse(responseBuilder.build(), entity);
        }
    }

    class GetLocationEventsResponse extends ResponseDelegate {

        private GetLocationEventsResponse(final Response response) {
            super(response);
        }

        private GetLocationEventsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetLocationEventsResponse respond200WithApplicationJson(final List<PrisonerSchedule> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationEventsResponse(responseBuilder.build(), entity);
        }

        public static GetLocationEventsResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationEventsResponse(responseBuilder.build(), entity);
        }

        public static GetLocationEventsResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationEventsResponse(responseBuilder.build(), entity);
        }

        public static GetLocationEventsResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationEventsResponse(responseBuilder.build(), entity);
        }
    }

    class GetActivitiesResponse extends ResponseDelegate {

        private GetActivitiesResponse(final Response response) {
            super(response);
        }

        private GetActivitiesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetActivitiesResponse respond200WithApplicationJson(final List<PrisonerSchedule> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetActivitiesResponse(responseBuilder.build(), entity);
        }
    }

    class GetAppointmentsResponse extends ResponseDelegate {

        private GetAppointmentsResponse(final Response response) {
            super(response);
        }

        private GetAppointmentsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAppointmentsResponse respond200WithApplicationJson(final List<PrisonerSchedule> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAppointmentsResponse(responseBuilder.build(), entity);
        }
    }

    class GetCourtEventsResponse extends ResponseDelegate {

        private GetCourtEventsResponse(final Response response) {
            super(response);
        }

        private GetCourtEventsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetCourtEventsResponse respond200WithApplicationJson(final List<PrisonerSchedule> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCourtEventsResponse(responseBuilder.build(), entity);
        }
    }

    class GetExternalTransfersResponse extends ResponseDelegate {

        private GetExternalTransfersResponse(final Response response) {
            super(response);
        }

        private GetExternalTransfersResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetExternalTransfersResponse respond200WithApplicationJson(final List<PrisonerSchedule> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetExternalTransfersResponse(responseBuilder.build(), entity);
        }
    }

    class GetVisitsResponse extends ResponseDelegate {

        private GetVisitsResponse(final Response response) {
            super(response);
        }

        private GetVisitsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetVisitsResponse respond200WithApplicationJson(final List<PrisonerSchedule> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetVisitsResponse(responseBuilder.build(), entity);
        }
    }
}
