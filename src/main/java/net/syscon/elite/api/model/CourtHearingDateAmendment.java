package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel(description = "Supports amendments to a scheduled prison to court hearing date/time for an offender.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtHearingDateAmendment {
    @ApiModelProperty(required = true, value = "The revised future date and time of the court hearing in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2022-12-01T14:40:00")
    @NotNull(message = "The revised future court hearing date time must be provided.")
    private LocalDateTime revisedHearingDateTime;
}
