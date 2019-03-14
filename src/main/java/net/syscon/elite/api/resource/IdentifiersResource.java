package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.OffenderIdentifier;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(tags = {"/identifiers"})
public interface IdentifiersResource {

    @GET
    @Path("/{type}/{value}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Identifiers for a specified type and value", notes = "Empty list will be returned for no matches", nickname = "getOffenderIdentifiersByTypeAndValue"
            , authorizations = {@Authorization("SYSTEM_USER")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderIdentifier.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    IdentifiersListResponse getOffenderIdentifiersByTypeAndValue(@ApiParam(value = "Identifier Type", example = "PNC", required = true) @PathParam("type") @NotNull String type,
                                                                        @ApiParam(value = "Identifier Value", example = "1234/XX", required = true) @PathParam("value") @NotNull String value);

    class IdentifiersListResponse extends ResponseDelegate {
        public IdentifiersListResponse(final Response response, final List<OffenderIdentifier> offenderIdentifiers) {
            super(response, offenderIdentifiers);
        }
    }
}
