package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Create external movement")
public class CreateExternalMovement {
    @Schema(description = "Booking id", required = true, example = "1")
    @NotNull
    private Long bookingId;
    @Schema(description = "Agency location moving from", required = true, example = "MDI")
    @NotNull
    private String fromAgencyId;
    @Schema(description = "Agency location moving to", required = true, example = "OUT")
    @NotNull
    private String toAgencyId;
    @Schema(description = "Date time of movement", required = true, example = "2020-02-28T14:40:00")
    @NotNull
    private LocalDateTime movementTime;
    @Schema(description = "Type of movement", required = true, example = "TRN")
    @NotNull
    private String movementType;
    @Schema(description = "Movement reason", required = true, example = "SEC")
    @NotNull
    private String movementReason;
    @Schema(description = "Direction code", required = true, example = "OUT", allowableValues = "IN,OUT")
    @NotNull
    private MovementDirection directionCode;
}
