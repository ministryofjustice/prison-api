package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.PersonIdentifier;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(tags = {"/persons"})
@SuppressWarnings("unused")
public interface PersonResource {

    @GET
    @Path("/{personId}/identifiers")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "The most recent value of each type of person identifier", notes = "The most recent value of each type of person identifier", nickname = "getPersonIdentifiers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PersonIdentifier.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetPersonIdentifiersResponse getPersonIdentifiers(@ApiParam(value = "The persons NOMIS identifier (personId).", required = true) @PathParam("personId") Long personId);

    class GetPersonIdentifiersResponse extends ResponseDelegate {

        private GetPersonIdentifiersResponse(final Response response) {
            super(response);
        }

        private GetPersonIdentifiersResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetPersonIdentifiersResponse respond200WithApplicationJson(final List<PersonIdentifier> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPersonIdentifiersResponse(responseBuilder.build(), entity);
        }

        public static GetPersonIdentifiersResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPersonIdentifiersResponse(responseBuilder.build(), entity);
        }

        public static GetPersonIdentifiersResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPersonIdentifiersResponse(responseBuilder.build(), entity);
        }

        public static GetPersonIdentifiersResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPersonIdentifiersResponse(responseBuilder.build(), entity);
        }
    }
}
