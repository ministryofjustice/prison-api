package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@ApiModel(description = "Detail for creating an appointment in bulk")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
public class AppointmentDetails {

    @ApiModelProperty(required=true, value="The Booking id of the offender for whom the appointment is to be created")
    @NotEmpty
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Date and time at which the appointment starts")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "Date and time at which the appointment ends")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "The Appointment's details")
    @Length(max = 4000)
    private String comment;
}
