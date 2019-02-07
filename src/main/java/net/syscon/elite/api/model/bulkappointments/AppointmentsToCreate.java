package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@ApiModel(description = "Details for creating appointments in bulk")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class AppointmentsToCreate {
    @ApiModelProperty(value = "The default values to be applied to each new appointment")
    @NotNull
    private AppointmentDefaults appointmentDefaults;

    @ApiModelProperty(value = "The details for creating each appointment.  A Missing value falls back to the default value if present")
    @NotNull
    private List<AppointmentDetails> appointments;

    public List<AppointmentDetails> withDefaults() {
        return appointments.stream().map(appt -> {
                    var builder = appt.toBuilder();
                    if (appt.getStartTime() == null) builder.startTime(appointmentDefaults.getStartTime());
                    if (appt.getEndTime() == null) builder.endTime(appointmentDefaults.getEndTime());
                    if (appt.getComment() == null) builder.comment(appointmentDefaults.getComment());
                    return builder.build();
                }
        ).collect(Collectors.toList());
    }
}
