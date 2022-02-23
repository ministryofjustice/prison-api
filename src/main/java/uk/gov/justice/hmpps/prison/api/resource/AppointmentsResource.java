package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "appointments")
@Validated
@RequestMapping("${api.base.path}/appointments")
@AllArgsConstructor
public class AppointmentsResource {
    private final AppointmentsService appointmentsService;

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The appointments have been created.")})
    @Operation(summary = "Create multiple appointments", description = "Create multiple appointments")
    @PostMapping
    @ProxyUser
    public List<CreatedAppointmentDetails> createAppointments(
        @RequestBody
        @Parameter(required = true) final AppointmentsToCreate createAppointmentsRequest
    ) {
        return appointmentsService.createAppointments(createAppointmentsRequest);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "The appointment has been deleted"),
        @ApiResponse(responseCode = "404", description = "The appointment was not found"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation")
    })
    @Operation(summary = "Delete an appointment.", description = "Delete appointment.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{appointmentId}")
    @HasWriteScope
    public void deleteAppointment(
        @PathVariable("appointmentId")
        @Parameter(description = "The unique identifier for the appointment", required = true)
        @NotNull final Long appointmentId
    ) {
        appointmentsService.deleteBookingAppointment(appointmentId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
    })
    @Operation(summary = "Delete multiple appointments.", description = "Delete multiple appointments.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/delete")
    @HasWriteScope
    public void deleteAppointments(
        @Parameter(description = "The unique identifier for the appointment", required = true)
        @NotNull @RequestBody List<Long> appointmentIds
    ) {
        appointmentsService.deleteBookingAppointments(appointmentIds);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The appointment has been returned"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "404", description = "The appointment was not found")
    })
    @Operation(summary = "Get an appointment by id.", description = "Get appointment byId.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{appointmentId}")
    public ScheduledEvent getAppointment(
        @PathVariable("appointmentId")
        @Parameter(description = "The unique identifier for the appointment", required = true)
        @NotNull final Long appointmentId
    ) {
        return appointmentsService.getBookingAppointment(appointmentId);
    }

    @Operation(summary = "Change an appointment's comment.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "The appointment's comment has been set."),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "404", description = "The appointment was not found."),
    })
    @HasWriteScope
    @PutMapping(path = "/{appointmentId}/comment", consumes = {MediaType.TEXT_PLAIN_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAppointmentComment(
        @PathVariable("appointmentId")
        @Parameter(description = "The appointment's unique identifier.", required = true)
        @NotNull final Long appointmentId,

        @RequestBody(required = false)
        @Parameter(description = "The text of the comment. May be empty or null", allowEmptyValue = true) final String comment
    ) {
        appointmentsService.updateComment(appointmentId, comment);
    }
}
