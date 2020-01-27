package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.bulkappointments.AppointmentsToCreate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api(tags = {"/appointments"})
public interface AppointmentsResource {
    @PostMapping
    @ApiOperation(value = "Create multiple appointments", notes = "Create multiple appointments", nickname = "createAppointments")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The appointments have been created.")})
    ResponseEntity<Void> createAppointments(@ApiParam(required = true) @RequestBody AppointmentsToCreate body);
}
