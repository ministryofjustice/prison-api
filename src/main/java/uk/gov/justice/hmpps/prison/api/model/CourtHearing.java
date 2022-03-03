package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@ApiModel(description = "Represents a court hearing for an offender court case.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class CourtHearing {

    @ApiModelProperty(value = "The court hearing identifier.", position = 1, example = "123456789")
    private Long id;

    @ApiModelProperty(value = "The date and start time of the court hearing in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", position = 2, example = "2020-02-28T14:40:00")
    private LocalDateTime dateTime;

    @ApiModelProperty(value = "The location of the court for the hearing.", position = 3)
    private Agency location;
}
