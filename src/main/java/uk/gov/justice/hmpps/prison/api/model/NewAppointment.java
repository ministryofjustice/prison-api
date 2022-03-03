package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Creation details for a new appointment
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Creation details for a new appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewAppointment {

    @ApiModelProperty(required = true, value = "Corresponds to the scheduled event subType")
    @Size(max = 12)
    @Pattern(regexp = "\\w*")
    @NotBlank
    private String appointmentType;

    @ApiModelProperty(required = true, value = "Location at which the appointment takes place.")
    @NotNull
    private Long locationId;

    @ApiModelProperty(required = true, value = "Date and time at which event starts")
    @NotNull
    private LocalDateTime startTime;

    @ApiModelProperty(value = "Date and time at which event ends")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "Details of appointment")
    @Size(max = 4000)
    private String comment;
}
