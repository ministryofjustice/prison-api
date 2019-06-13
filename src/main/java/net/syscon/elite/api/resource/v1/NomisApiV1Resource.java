package net.syscon.elite.api.resource.v1;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.v1.Location;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Api(tags = {"/v1"})
@Deprecated
public interface NomisApiV1Resource {

    @GET
    @Path("/offenders/{nomsId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Since the offender's location can change often and is fairly sensitive (and therefore should not automatically be exposed to all services), this information is not included in the general offender information call.",
            notes = "The levels shows the type of each level of the location address as defined on the Agency Details tab in Maintain Agency Locations screen (OUMAGLOC).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    LatestBookingLocationResponse getLatestBookingLocation(@ApiParam(value = "nomsId", example = "A1417AE", required = true) @PathParam("nomsId") @NotNull String nomsId);

    class LatestBookingLocationResponse extends ResponseDelegate {
        public LatestBookingLocationResponse(final Response response, final Location location) {
            super(response, location);
        }
    }


}
