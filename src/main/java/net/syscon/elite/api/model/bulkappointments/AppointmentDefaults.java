package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@ApiModel(description = "Default values to be applied when creating each appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class AppointmentDefaults {
    @ApiModelProperty(required = true, value = "The scheduled event subType")
    @Length(max = 12)
    @Pattern(regexp = "\\w*")
    private String appointmentType;

    @ApiModelProperty(required = true, value = "The Location at which the appointment takes place")
    private Long locationId;

    @ApiModelProperty(required = true, value = "The date and time at which the appointment starts")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "The date and time at which the appointment ends (optional)")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "The appointment's details")
    @Length(max = 4000)
    private String comment;
}
