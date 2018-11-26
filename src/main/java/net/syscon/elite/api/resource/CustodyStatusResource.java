package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.Movement;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = {"/custody-statuses"})
@SuppressWarnings("unused")
public interface CustodyStatusResource {

    @GET
    @Path("/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Deprecated
    @ApiOperation(value = "Returns a list of recently released or moved offender nos and the associated timestamp.", notes = "Deprecated: Use <b>/movements/</b> instead. This API will be removed in a future release.", nickname="getRecentMovements")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Movement.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetRecentMovementsResponse getRecentMovements(@ApiParam(value = "A timestamp that indicates the earliest record required", required = true) @QueryParam("fromDateTime") LocalDateTime fromDateTime,
                                                  @ApiParam(value = "The date for which movements are searched", required = true) @QueryParam("movementDate") LocalDate movementDate);

    class GetRecentMovementsResponse extends ResponseDelegate {

        private GetRecentMovementsResponse(Response response) { super(response); }
        private GetRecentMovementsResponse(Response response, Object entity) { super(response, entity); }

        public static GetRecentMovementsResponse respond200WithApplicationJson(List<Movement> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsResponse(responseBuilder.build(), entity);
        }

        public static GetRecentMovementsResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsResponse(responseBuilder.build(), entity);
        }

        public static GetRecentMovementsResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsResponse(responseBuilder.build(), entity);
        }

        public static GetRecentMovementsResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRecentMovementsResponse(responseBuilder.build(), entity);
        }
    }
}
