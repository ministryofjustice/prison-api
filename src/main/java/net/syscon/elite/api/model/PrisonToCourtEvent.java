package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Represents the data required to schedule a prison to court event for an offender.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrisonToCourtEvent {

    @ApiModelProperty(required = true, value = "The court case identifier to link the event to.", position = 1, example = "1")
    private Long courtCaseId;

    @ApiModelProperty(required = true, value = "The prison agency code where the offender will be moved from.", position = 2, example = "LEI")
    private String fromPrisonLocation;

    @ApiModelProperty(required = true, value = "The court agency code where the offender will moved to.", position = 3, example = "LEEDSCC")
    private String toCourtLocation;

    @ApiModelProperty(required = true, value = "The date and time of the court event.", position = 4, example = "2020-02-28T14:40:00.000Z")
    private String courtEventDateTime;
}
