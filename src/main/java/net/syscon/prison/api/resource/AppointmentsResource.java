package net.syscon.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.syscon.prison.api.model.bulkappointments.AppointmentsToCreate;
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
