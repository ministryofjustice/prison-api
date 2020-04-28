package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@ApiModel(description = "Represents the data required for a prison to prison move.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class PrisonToPrisonMove {

    @ApiModelProperty(required = true, value = "The prison (agency code) to be moved from.", position = 1, example = "LEI")
    @NotBlank(message = "The from prison location must be provided.")
    @Size(max = 6, message = "From location must be a maximum of 6 characters.")
    private String fromPrison;

    @ApiModelProperty(required = true, value = "The prison (agency code) to be moved to.", position = 2, example = "PVI")
    @NotBlank(message = "The to prison location must be provided.")
    @Size(max = 6, message = "To location must be a maximum of 6 characters.")
    private String toPrison;

    @ApiModelProperty(required = true, value = "The escort type of the move.", position = 3, example = "PECS")
    @NotBlank(message = "The escort type must be provided.")
    @Size(max = 12, message = "To escort type must be a maximum of 12 characters.")
    private String escortType;

    @ApiModelProperty(required = true, value = "The date and time of the move.", position = 4, example = "2020-02-28T14:40:00.000Z")
    @NotNull(message = "The move date time must be provided.")
    private LocalDateTime scheduledMoveDateTime;
}
