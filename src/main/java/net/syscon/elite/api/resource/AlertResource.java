package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.AlertType;
import net.syscon.elite.api.model.AlertSubtype;
import net.syscon.elite.api.model.ErrorResponse;

import javax.ws.rs.*;
import java.util.List;

@Api(tags = {"/alerts"})
public interface AlertResource {

    @GET
    @Path("/types")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return all active alert types", notes = "Return all active alert types",
            nickname = "getAlertTypes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AlertType.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<AlertType> getAlertTypes();

    @GET
    @Path("/subtypes")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return all active alert subtypes", notes = "Return all active alert subtypes",
            nickname = "getAlertTypes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AlertSubtype.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<AlertSubtype> getAlertSubtypes(@ApiParam(value = "Parent code to filter by.") @QueryParam("parentCode") String parentCode);
}
