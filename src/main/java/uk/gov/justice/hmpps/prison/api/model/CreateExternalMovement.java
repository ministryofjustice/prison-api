package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Create external movement")
public class CreateExternalMovement {
    @ApiModelProperty(value = "Booking id", required = true, example = "1")
    @NotNull
    private Long bookingId;
    @ApiModelProperty(value = "Agency location moving from", required = true, example = "MDI")
    @NotNull
    private String fromAgencyId;
    @ApiModelProperty(value = "Agency location moving to", required = true, example = "OUT")
    @NotNull
    private String toAgencyId;
    @ApiModelProperty(value = "Date time of movement", required = true, example = "2020-02-28T14:40:00")
    @NotNull
    private LocalDateTime movementTime;
    @ApiModelProperty(value = "Type of movement", required = true, example = "TRN")
    @NotNull
    private String movementType;
    @ApiModelProperty(value = "Movement reason", required = true, example = "SEC")
    @NotNull
    private String movementReason;
    @ApiModelProperty(value = "Direction code", required = true, example = "OUT", allowableValues = "IN,OUT")
    @NotNull
    private MovementDirection directionCode;
}
