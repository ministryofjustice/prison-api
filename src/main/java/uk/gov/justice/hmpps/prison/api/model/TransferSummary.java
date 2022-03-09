package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@SuppressWarnings("unused")
@ApiModel(description = "The container object for transfer and movement events")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferSummary {

    @ApiModelProperty(value = "List of scheduled or completed court events")
    private List<CourtEvent> courtEvents;

    @ApiModelProperty(value = "List of scheduled or completed offender events")
    private List<TransferEvent> transferEvents;

    @ApiModelProperty(value = "List of scheduled or completed release events")
    private List<ReleaseEvent> releaseEvents;

    @ApiModelProperty(value = "List of confirmed movements")
    private List<MovementSummary> movements;
}