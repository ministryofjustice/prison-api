package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(tags = {"/offender-relationships"})
@SuppressWarnings("unused")
public interface OffenderRelationshipResource {

    @GET
    @Path("/externalRef/{externalRef}/{relationshipType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of offenders", notes = "List of offenders", nickname="getBookingsByExternalRefAndType")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = OffenderSummary.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetBookingsByExternalRefAndTypeResponse getBookingsByExternalRefAndType(@ApiParam(value = "External Unique Reference to Contact Person", required = true) @PathParam("externalRef") String externalRef,
                                                                            @ApiParam(value = "Relationship Type", required = true) @PathParam("relationshipType") String relationshipType);

    @GET
    @Path("/person/{personId}/{relationshipType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of offenders that are related to this person Id and relationship type", notes = "List of offenders that are related to this person Id and relationship type", nickname="getBookingsByPersonIdAndType")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OffenderSummary.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetBookingsByPersonIdAndTypeResponse getBookingsByPersonIdAndType(@ApiParam(value = "Person Id of the contact person", required = true) @PathParam("personId") Long personId,
                                                                      @ApiParam(value = "Relationship Type", required = true) @PathParam("relationshipType") String relationshipType);

    class GetBookingsByExternalRefAndTypeResponse extends ResponseDelegate {

        private GetBookingsByExternalRefAndTypeResponse(Response response) { super(response); }
        private GetBookingsByExternalRefAndTypeResponse(Response response, Object entity) { super(response, entity); }

        public static GetBookingsByExternalRefAndTypeResponse respond200WithApplicationJson(List<OffenderSummary> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingsByExternalRefAndTypeResponse(responseBuilder.build(), entity);
        }

        public static GetBookingsByExternalRefAndTypeResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingsByExternalRefAndTypeResponse(responseBuilder.build(), entity);
        }

        public static GetBookingsByExternalRefAndTypeResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingsByExternalRefAndTypeResponse(responseBuilder.build(), entity);
        }

        public static GetBookingsByExternalRefAndTypeResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingsByExternalRefAndTypeResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingsByPersonIdAndTypeResponse extends ResponseDelegate {

        private GetBookingsByPersonIdAndTypeResponse(Response response) { super(response); }
        private GetBookingsByPersonIdAndTypeResponse(Response response, Object entity) { super(response, entity); }

        public static GetBookingsByPersonIdAndTypeResponse respond200WithApplicationJson(List<OffenderSummary> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingsByPersonIdAndTypeResponse(responseBuilder.build(), entity);
        }

        public static GetBookingsByPersonIdAndTypeResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingsByPersonIdAndTypeResponse(responseBuilder.build(), entity);
        }

        public static GetBookingsByPersonIdAndTypeResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingsByPersonIdAndTypeResponse(responseBuilder.build(), entity);
        }

        public static GetBookingsByPersonIdAndTypeResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingsByPersonIdAndTypeResponse(responseBuilder.build(), entity);
        }
    }
}
