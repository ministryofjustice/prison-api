package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@ApiModel(description = "Represents the data required to schedule a prison to court hearing for an offender.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrisonToCourtHearing {

    @ApiModelProperty(required = true, value = "The court case identifier to link the hearing to.", position = 1, example = "1")
    @NotNull(message = "The court case identifier must be provided.")
    private Long courtCaseId;

    @ApiModelProperty(required = true, value = "The prison (agency code) where the offender will be moved from.", position = 2, example = "LEI")
    @NotBlank(message = "The from prison location must be provided.")
    @Size(max = 6, message = "From location must be a maximum of 6 characters.")
    private String fromPrisonLocation;

    @ApiModelProperty(required = true, value = "The court (agency code) where the offender will moved to.", position = 3, example = "LEEDSCC")
    @NotBlank(message = "The court location to be moved to must be provided.")
    @Size(max = 6, message = "To location must be a maximum of 6 characters.")
    private String toCourtLocation;

    @ApiModelProperty(required = true, value = "The date and time of the court hearing.", position = 4, example = "2020-02-28T14:40:00.000Z")
    @NotBlank(message = "The court hearing date time must be provided.")
    private LocalDateTime courtHearingDateTime;

    @ApiModelProperty(value = "Any comments related to the court case.", position = 5, example = "Restricted access to parking level.")
    @Size(max = 240, message = "Comment text must be a maximum of 240 characters.")
    private String comments;
}
