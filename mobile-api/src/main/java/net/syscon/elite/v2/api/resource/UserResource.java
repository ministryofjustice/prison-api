package net.syscon.elite.v2.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.v2.api.model.ErrorResponse;
import net.syscon.elite.v2.api.model.Location;
import net.syscon.elite.v2.api.support.ResponseDelegate;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "User Resource", produces = MediaType.APPLICATION_JSON)
@Path("/v2/users")
public interface UserResource {
    @GET
    @Path("/me/locations")
    @Produces("application/json")
    @ApiOperation(value = "Gets list of locations applicable for user.", nickname = "getUserLocations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Locations successfully retrieved.", response = Location.class, responseContainer = "List",
                    responseHeaders = {
                            @ResponseHeader(name = "Total-Records", description = "Total number of records available", response = Long.class),
                            @ResponseHeader(name = "Page-Offset", description = "Offset of first returned record", response = Long.class),
                            @ResponseHeader(name = "Page-Limit", description = "Limit for number of records returned", response = Long.class)
                    })
    })
    GetUsersMeLocationsResponse getUsersMeLocations(@ApiParam(value = "Offset of first returned record") @HeaderParam("Page-Offset") Long offset,
                                                    @ApiParam(value = "Limit for number of records returned") @HeaderParam("Page-Limit") Long limit);

    class GetUsersMeLocationsResponse extends ResponseDelegate {
        private GetUsersMeLocationsResponse(Response response, Object entity) {
            super(response, entity);
        }

        private GetUsersMeLocationsResponse(Response response) {
            super(response);
        }

        public static GetUsersMeLocationsResponse respond200WithApplicationJson(List<Location> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new GetUsersMeLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetUsersMeLocationsResponse respond400WithApplicationJson(ErrorResponse entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new GetUsersMeLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetUsersMeLocationsResponse respond404WithApplicationJson(ErrorResponse entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new GetUsersMeLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetUsersMeLocationsResponse respond500WithApplicationJson(ErrorResponse entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new GetUsersMeLocationsResponse(responseBuilder.build(), entity);
        }
    }
}
