package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = {"/movements"})
@SuppressWarnings("unused")
public interface MovementResource {

    @GET
    @Path("/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Returns a list of recently released or moved offender nos and the associated timestamp.", notes = "Returns a list of recently released or moved offender nos and the associated timestamp.", nickname="getRecentMovementsByDate")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Movement.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetRecentMovementsByDateResponse getRecentMovementsByDate(@ApiParam(value = "A timestamp that indicates the earliest record required", required = true) @QueryParam("fromDateTime") LocalDateTime fromDateTime,
                                                              @ApiParam(value = "The date for which movements are searched", required = true) @QueryParam("movementDate") LocalDate movementDate,
                                                              @ApiParam(value = "Filter to just movements to or from this agency.") @QueryParam("agencyId") String agencyId);

    @GET
    @Path("/rollcount/{agencyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Current establishment rollcount numbers.", notes = "Current establishment rollcount numbers.", nickname="getRollcount")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = RollCount.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetRollcountResponse getRollcount(@ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
                                      @ApiParam(value = "If false return data for prisoners in cell locations, if true return unassigned prisoners, i.e. those in non-cell locations.", defaultValue = "false") @QueryParam("unassigned") boolean unassigned);

    @GET
    @Path("/rollcount/{agencyId}/movements")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Rollcount movement numbers.", notes = "Rollcount movement numbers.", nickname="getRollcountMovements")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MovementCount.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })
    GetRollcountMovementsResponse getRollcountMovements(@ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
                                                        @ApiParam(value = "The date for which movements are counted, default today.", required = true) @QueryParam("movementDate") LocalDate movementDate);

    @GET
    @Path("/{agencyId}/in/{isoDate}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Information on offenders in today.", notes = "Information on offenders in on given date.", nickname="getMovementsIn")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })
    GetMovementsInTodayResponse getMovementsIn(@ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
                                               @ApiParam(value = "date", required = true) @PathParam("isoDate") LocalDate movementsDate);

    @POST
    @Path("/offenders")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", nickname="getRecentMovementsByOffenders")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = Movement.class, responseContainer = "List") })
    GetRecentMovementsByOffendersResponse getRecentMovementsByOffenders(@ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                                                        @ApiParam(value = "movement type codes to filter by") @QueryParam("movementTypes") List<String> movementTypes);
    @GET
    @Path("/{agencyId}/enroute")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Enroute prisoner movement details.", notes = "Enroute to reception", nickname="getEnrouteOffenderMovements")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })
    GetEnrouteOffenderMovementsResponse getEnrouteOffenderMovements(@ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
                                                                    @ApiParam(value = "The date for which enroute movements are counted, default is today.", required = true) @QueryParam("movementDate") LocalDate movementDate,
                                                                    @ApiParam(value = "Comma separated list of one or more of the following fields - <b>bookingId, offenderNo, firstName, lastName - defaults to lastName, firstName</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                    @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/rollcount/{agencyId}/enroute")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Enroute prisoner movement count.", notes = "Enroute to reception count", nickname="getEnrouteOffenderMovementCount")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })
    GetEnrouteOffenderMovementCountResponse getEnrouteOffenderMovementCount(@ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId, @ApiParam(value = "The date for which enroute movements are counted, default today.", required = true) @QueryParam("movementDate") LocalDate movementDate);

    @GET
    @Path("/{agencyId}/out/{isoDate}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", nickname="getOffendersOut")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderOutTodayDto.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    GetOffendersOutTodayResponse getOffendersOutToday(@ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
                                                      @ApiParam(value = "date", required = true) @PathParam("isoDate") LocalDate movementsDate);


    class GetRecentMovementsByDateResponse extends ResponseDelegate {

        private GetRecentMovementsByDateResponse(Response response) { super(response); }
        private GetRecentMovementsByDateResponse(Response response, Object entity) { super(response, entity); }

        public static GetRecentMovementsByDateResponse respond200WithApplicationJson(List<Movement> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsByDateResponse(responseBuilder.build(), entity);
        }

        public static GetRecentMovementsByDateResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsByDateResponse(responseBuilder.build(), entity);
        }

        public static GetRecentMovementsByDateResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsByDateResponse(responseBuilder.build(), entity);
        }

        public static GetRecentMovementsByDateResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsByDateResponse(responseBuilder.build(), entity);
        }
    }

    class GetRollcountResponse extends ResponseDelegate {

        private GetRollcountResponse(Response response) { super(response); }
        private GetRollcountResponse(Response response, Object entity) { super(response, entity); }

        public static GetRollcountResponse respond200WithApplicationJson(List<RollCount> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRollcountResponse(responseBuilder.build(), entity);
        }

        public static GetRollcountResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRollcountResponse(responseBuilder.build(), entity);
        }

        public static GetRollcountResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRollcountResponse(responseBuilder.build(), entity);
        }

        public static GetRollcountResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRollcountResponse(responseBuilder.build(), entity);
        }
    }

    class GetRollcountMovementsResponse extends ResponseDelegate {

        private GetRollcountMovementsResponse(Response response) { super(response); }
        private GetRollcountMovementsResponse(Response response, Object entity) { super(response, entity); }

        public static GetRollcountMovementsResponse respond200WithApplicationJson(MovementCount entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRollcountMovementsResponse(responseBuilder.build(), entity);
        }

        public static GetRollcountMovementsResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRollcountMovementsResponse(responseBuilder.build(), entity);
        }

        public static GetRollcountMovementsResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRollcountMovementsResponse(responseBuilder.build(), entity);
        }

        public static GetRollcountMovementsResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRollcountMovementsResponse(responseBuilder.build(), entity);
        }
    }

    class GetMovementsInTodayResponse extends ResponseDelegate {

        private GetMovementsInTodayResponse(Response response) { super(response); }
        private GetMovementsInTodayResponse(Response response, Object entity) { super(response, entity); }

        public static GetMovementsInTodayResponse respond200WithApplicationJson(List<OffenderIn> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMovementsInTodayResponse(responseBuilder.build(), entity);
        }

        public static GetMovementsInTodayResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMovementsInTodayResponse(responseBuilder.build(), entity);
        }

        public static GetMovementsInTodayResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMovementsInTodayResponse(responseBuilder.build(), entity);
        }

        public static GetMovementsInTodayResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMovementsInTodayResponse(responseBuilder.build(), entity);
        }
    }


    class GetRecentMovementsByOffendersResponse extends ResponseDelegate {

        private GetRecentMovementsByOffendersResponse(Response response) { super(response); }
        private GetRecentMovementsByOffendersResponse(Response response, Object entity) { super(response, entity); }

        public static GetRecentMovementsByOffendersResponse respond200WithApplicationJson(List<Movement> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsByOffendersResponse(responseBuilder.build(), entity);
        }
    }

    class GetEnrouteOffenderMovementsResponse extends ResponseDelegate {

        private GetEnrouteOffenderMovementsResponse(Response response) { super(response); }
        private GetEnrouteOffenderMovementsResponse(Response response, Object entity) { super(response, entity); }

        public static GetEnrouteOffenderMovementsResponse respond200WithApplicationJson(List<OffenderMovement> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetEnrouteOffenderMovementsResponse(responseBuilder.build(), entity);
        }
    }

    class GetEnrouteOffenderMovementCountResponse extends ResponseDelegate {

        private GetEnrouteOffenderMovementCountResponse(Response response) { super(response); }
        private GetEnrouteOffenderMovementCountResponse(Response response, Object entity) { super(response, entity); }

        public static GetEnrouteOffenderMovementCountResponse respond200WithApplicationJson(int entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetEnrouteOffenderMovementCountResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffendersOutTodayResponse extends ResponseDelegate {
        private GetOffendersOutTodayResponse(Response response) { super(response); }
        private GetOffendersOutTodayResponse(Response response, Object entity) {  super(response, entity); }

        public static GetOffendersOutTodayResponse respond200WithApplicationJson(List<OffenderOutTodayDto> entity) {
            return new GetOffendersOutTodayResponse(ResponseUtil.applicationJson(entity, 200), entity);
        }
    }

    class ResponseUtil {
       static Response  applicationJson(Object entity, Integer code) {
           Response.ResponseBuilder responseBuilder = Response.status(code)
                   .header("Content-Type", MediaType.APPLICATION_JSON);
           responseBuilder.entity(entity);

           return responseBuilder.build();
       }
    }
}
