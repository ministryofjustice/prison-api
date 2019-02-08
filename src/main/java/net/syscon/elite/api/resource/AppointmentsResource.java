package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.bulkappointments.AppointmentsToCreate;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Api(tags = {"/appointments"})
public interface AppointmentsResource {
    @POST
    @Path("/")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Create multiple appointments", notes = "Create multiple appointments", nickname="createAppointments")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The appointments have been created.") })
    Response createAppointments(@ApiParam(required = true) AppointmentsToCreate body);
}
