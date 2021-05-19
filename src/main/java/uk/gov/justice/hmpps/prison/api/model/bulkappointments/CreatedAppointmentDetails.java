package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@ApiModel(description = "The details of an appointment that has just been created")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreatedAppointmentDetails {

    @ApiModelProperty(required = true, value = "The id of the appointment that was created.", example = "123456")
    @NotNull
    private Long appointmentEventId;

    @ApiModelProperty(required = true, value = "The Booking id of the offender for whom the appointment was created.", example = "123456")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(value = "The start time of the appointment.", example = "2018-12-31T23:50")
    @Future
    private LocalDateTime startTime;

    @ApiModelProperty(value = "The end time of the appointment.", example = "2018-12-31T23:59")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "The ids of all recurring appointments that were created.", example = "2018-12-31T23:59")
    private List<Long> recurringAppointmentEventIds;
}
