package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;

import javax.validation.constraints.NotNull;

@RestController
@Api(tags = {"appointments"})
@Validated
@RequestMapping("${api.base.path}/appointments")
@AllArgsConstructor
public class AppointmentsResource {
    private final AppointmentsService appointmentsService;

    @ApiResponses({
            @ApiResponse(code = 200, message = "The appointments have been created.")})
    @ApiOperation(value = "Create multiple appointments", notes = "Create multiple appointments", nickname = "createAppointments")
    @PostMapping
    @ProxyUser
    public ResponseEntity<Void> createAppointments(@RequestBody @ApiParam(required = true) final AppointmentsToCreate createAppointmentsRequest) {
        appointmentsService.createAppointments(createAppointmentsRequest);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 204, message = "The appointment has been deleted")})
    @ApiOperation(value = "Delete an appointment .", notes = "Delete appointment.", nickname = "deleteBookingAppointment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{eventId}")
    @HasWriteScope
    public void deleteAppointment(@PathVariable("eventId") @ApiParam(value = "The unique identifier for the appointment", required = true) @NotNull final Long eventId) {
        appointmentsService.deleteBookingAppointment(eventId);
    }


}
