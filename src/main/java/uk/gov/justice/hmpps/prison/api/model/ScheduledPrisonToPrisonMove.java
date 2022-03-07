package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "Represents the data for a scheduled prison to prison move.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ScheduledPrisonToPrisonMove {

    @ApiModelProperty(value = "The identifier for the scheduled prison to prison move.", position = 1, example = "123456789")
    private Long id;

    @ApiModelProperty(value = "The date and start time of the move in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", position = 2, example = "2020-02-28T14:40:00")
    private LocalDateTime scheduledMoveDateTime;

    @ApiModelProperty(value = "The location of the prison to move from.", position = 3, example = "PVI")
    private Agency fromPrisonLocation;

    @ApiModelProperty(value = "The location of the prison to move to.", position = 4, example = "LEI")
    private Agency toPrisonLocation;
}
