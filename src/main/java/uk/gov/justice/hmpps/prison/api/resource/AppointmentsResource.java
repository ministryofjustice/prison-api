package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.CreatedAppointmentDetails;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;

import javax.validation.constraints.NotNull;
import java.util.List;

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
    public List<CreatedAppointmentDetails> createAppointments(
        @RequestBody
        @ApiParam(required = true) final AppointmentsToCreate createAppointmentsRequest
    ) {
        return appointmentsService.createAppointments(createAppointmentsRequest);
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = "The appointment has been deleted"),
        @ApiResponse(code = 404, message = "The appointment was not found"),
        @ApiResponse(code = 403, message = "The client is not authorised for this operation")
    })
    @ApiOperation(value = "Delete an appointment.", notes = "Delete appointment.", nickname = "deleteBookingAppointment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{appointmentId}")
    @HasWriteScope
    public void deleteAppointment(
        @PathVariable("appointmentId")
        @ApiParam(value = "The unique identifier for the appointment", required = true)
        @NotNull final Long appointmentId
    ) {
        appointmentsService.deleteBookingAppointment(appointmentId);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "The appointment has been returned"),
        @ApiResponse(code = 403, message = "The client is not authorised for this operation"),
        @ApiResponse(code = 404, message = "The appointment was not found")
    })
    @ApiOperation(value = "Get an appointment by id.", notes = "Get appointment byId.", nickname = "getBookingAppointment")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{appointmentId}")
    public ScheduledEvent getAppointment(
        @PathVariable("appointmentId")
        @ApiParam(value = "The unique identifier for the appointment", required = true)
        @NotNull final Long appointmentId
    ) {
        return appointmentsService.getBookingAppointment(appointmentId);
    }

    @ApiOperation(value = "Change an appointment's comment.")
    @ApiResponses({
        @ApiResponse(code = 204, message = "The appointment's comment has been set."),
        @ApiResponse(code = 403, message = "The client is not authorised for this operation"),
        @ApiResponse(code = 404, message = "The appointment was not found."),
    })
    @HasWriteScope
    @PutMapping(path = "/{appointmentId}/comment", consumes = {MediaType.TEXT_PLAIN_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAppointmentComment(
        @PathVariable("appointmentId")
        @ApiParam(value = "The appointment's unique identifier.", required = true)
        @NotNull final Long appointmentId,

        @RequestBody(required = false)
        @ApiParam(value = "The text of the comment. May be empty or null", allowEmptyValue = true) final String comment
    ) {
        appointmentsService.updateComment(appointmentId, comment);
    }
}
