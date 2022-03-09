package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "Represents the data required for scheduling a prison to prison move.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class SchedulePrisonToPrisonMove {

    @Schema(required = true, description = "The prison (agency code) to be moved from.", example = "LEI")
    @NotBlank(message = "The from prison location must be provided.")
    @Size(max = 6, message = "From prison must be a maximum of 6 characters.")
    private String fromPrisonLocation;

    @Schema(required = true, description = "The prison (agency code) to be moved to.", example = "PVI")
    @NotBlank(message = "The to prison location must be provided.")
    @Size(max = 6, message = "To prison must be a maximum of 6 characters.")
    private String toPrisonLocation;

    @Schema(required = true, description = "The escort type of the move.", example = "PECS")
    @NotBlank(message = "The escort type must be provided.")
    @Size(max = 12, message = "Escort type must be a maximum of 12 characters.")
    private String escortType;

    @Schema(required = true, description = "The date and time of the move in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2020-02-28T14:40:00")
    @NotNull(message = "The move date time must be provided.")
    private LocalDateTime scheduledMoveDateTime;
}
