package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Create external movement")
public class CreateExternalMovement {
    @Schema(description = "Booking id", requiredMode = REQUIRED, example = "1")
    @NotNull
    private Long bookingId;
    @Schema(description = "Agency location moving from", requiredMode = REQUIRED, example = "MDI")
    @NotNull
    private String fromAgencyId;
    @Schema(description = "Agency location moving to", requiredMode = REQUIRED, example = "OUT")
    @NotNull
    private String toAgencyId;
    @Schema(description = "Date time of movement", requiredMode = REQUIRED, example = "2020-02-28T14:40:00")
    @NotNull
    private LocalDateTime movementTime;
    @Schema(description = "Type of movement", requiredMode = REQUIRED, example = "TRN")
    @NotNull
    private String movementType;
    @Schema(description = "Movement reason", requiredMode = REQUIRED, example = "SEC")
    @NotNull
    private String movementReason;
    @Schema(description = "Direction code", requiredMode = REQUIRED, example = "OUT")
    @NotNull
    private MovementDirection directionCode;
}
