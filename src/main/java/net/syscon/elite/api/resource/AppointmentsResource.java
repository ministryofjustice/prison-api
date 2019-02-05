package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.bulkappointments.CreateAppointmentsOutcomes;
import net.syscon.elite.api.model.bulkappointments.NewAppointments;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Api(tags = {"/appointments"})
public interface AppointmentsResource {
    @POST
    @Path("/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create multiple appointments", notes = "Create multiple appointments", nickname="createMultipleAppointments")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The request has been processed.  Outcomes are returned in the response", response = CreateAppointmentsOutcomes.class) })
    CreateAppointmentsOutcomes createAppointments(@ApiParam(required = true) NewAppointments body);
}
