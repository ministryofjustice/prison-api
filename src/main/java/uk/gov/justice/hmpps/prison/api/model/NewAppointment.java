package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Creation details for a new appointment
 **/
@SuppressWarnings("unused")
@Schema(description = "Creation details for a new appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewAppointment {

    @Schema(required = true, description = "Corresponds to the scheduled event subType")
    @Size(max = 12)
    @Pattern(regexp = "\\w*")
    @NotBlank
    private String appointmentType;

    @Schema(required = true, description = "Location at which the appointment takes place.")
    @NotNull
    private Long locationId;

    @Schema(required = true, description = "Date and time at which event starts")
    @NotNull
    private LocalDateTime startTime;

    @Schema(description = "Date and time at which event ends")
    private LocalDateTime endTime;

    @Schema(description = "Details of appointment")
    @Size(max = 4000)
    private String comment;
}
