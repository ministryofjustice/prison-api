package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Default values to be applied when creating each appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDefaults {
    @Schema(requiredMode = REQUIRED, description = "The scheduled event subType", example = "ACTI")
    @Size(max = 12)
    @Pattern(regexp = "\\w*")
    @NotEmpty
    private String appointmentType;

    @Schema(requiredMode = REQUIRED, description = "The identifier of the appointments' Location. The location must be situated in the requestor's case load.", example = "25")
    @NotNull
    private Long locationId;

    @Schema(requiredMode = REQUIRED, description = "The date and time at which the appointments start. ISO 8601 Date-time format. startTime must be in the future.", example = "2018-12-31T14:00")
    @NotNull
    @Future
    private LocalDateTime startTime;

    @Schema(description = "The date and time at which the appointments end. ISO 8601 Date-time format. endTime, if present, must be later than startTime.", example = "2018-12-31T14:50:00")
    private LocalDateTime endTime;

    @Schema(description = "A comment that applies to all the appointments in this request.", example = "Please provide helpful supporting text when it applies to all the appointments specified by this request.")
    @Size(max = 4000)
    private String comment;
}
