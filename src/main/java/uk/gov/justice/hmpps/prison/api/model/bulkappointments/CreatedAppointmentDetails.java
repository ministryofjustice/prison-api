package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "The details of an appointment that has just been created")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreatedAppointmentDetails {
    @Schema(requiredMode = REQUIRED, description = "The Booking id of the offender for whom the appointment was created.", example = "123456")
    @NotNull
    private Long bookingId;

    @Schema(description = "The start time of the appointment.", example = "2018-12-31T23:50")
    @Future
    private LocalDateTime startTime;

    @Schema(description = "The end time of the appointment.", example = "2018-12-31T23:59")
    private LocalDateTime endTime;

    private Long appointmentEventId;

    @Schema(description = "The scheduled event subType", example = "ACTI")
    private String appointmentType;

    @Schema(description = "The identifier of the appointments' Location. The location must be situated in the requestor's case load.", example = "25")
    @NotNull
    private Long locationId;
}
