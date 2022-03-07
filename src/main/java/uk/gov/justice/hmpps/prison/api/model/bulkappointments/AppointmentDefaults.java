package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@ApiModel(description = "Default values to be applied when creating each appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDefaults {
    @ApiModelProperty(required = true, value = "The scheduled event subType", example = "ACTI")
    @Size(max = 12)
    @Pattern(regexp = "\\w*")
    @NotEmpty
    private String appointmentType;

    @ApiModelProperty(required = true, value = "The identifier of the appointments' Location. The location must be situated in the requestor's case load.", example = "25", position = 1)
    @NotNull
    private Long locationId;

    @ApiModelProperty(required = true, value = "The date and time at which the appointments start. ISO 8601 Date-time format. startTime must be in the future.", example = "2018-12-31T14:00", position = 2)
    @NotNull
    @Future
    private LocalDateTime startTime;

    @ApiModelProperty(value = "The date and time at which the appointments end. ISO 8601 Date-time format. endTime, if present, must be later than startTime.", example = "2018-12-31T14:50:00", position = 3)
    private LocalDateTime endTime;

    @ApiModelProperty(value = "A comment that applies to all the appointments in this request.", example = "Please provide helpful supporting text when it applies to all the appointments specified by this request.", position = 4)
    @Size(max = 4000)
    private String comment;
}
