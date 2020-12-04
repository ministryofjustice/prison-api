package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
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
    public ResponseEntity<Void> createAppointments(
        @RequestBody
        @ApiParam(required = true)
        final AppointmentsToCreate createAppointmentsRequest
    ) {
        appointmentsService.createAppointments(createAppointmentsRequest);
        return ResponseEntity.ok().build();
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
        @NotNull
        final Long appointmentId
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
    @GetMapping(path = "/{appointmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ScheduledEvent getAppointment(
        @PathVariable("appointmentId")
        @ApiParam(value = "The unique identifier for the appointment", required = true)
        @NotNull
        final Long appointmentId
    ) {
        return appointmentsService.getBookingAppointment(appointmentId);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "All appointments of the specified type occurring on the given date have been returned"),
        @ApiResponse(code = 403, message = "The client is not authorised for this operation")
    })
    @ApiOperation(value = "Get all appointments of the specified type which occur on the given date.", notes = "Get appointments by type and day", nickname = "getBookingAppointmentsByTypeAndDay")
    @GetMapping(path = "/of-type/{type}/on-date/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ScheduledEvent> getAppointments(
        @PathVariable(value = "date")
        @DateTimeFormat(iso = ISO.DATE)
        @ApiParam(value = "Restrict to appointments placed on this date. ISO-8601 date format (yyyy-MM-dd)", example = "2020-12-25")
        @NotNull
        final LocalDate date,

        @PathVariable(value = "type")
        @ApiParam(value = "Restrict to appointments of this type", example = "VLB")
        @NotBlank
        final String type
    ) {
        return appointmentsService.getBookingAppointmentsByTypeAndDate(type, date);
    }
}
