package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@ApiModel(description = "Details for creating appointments in bulk")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class AppointmentsToCreate {
    @ApiModelProperty(required = true, value = "The default values to be applied to each new appointment. An individual appointment may change the startTime, add or change the endTime and provide text for that appointment's comment.")
    @NotNull
    @Valid
    private AppointmentDefaults appointmentDefaults;

    @ApiModelProperty(required = true, value = "The details for creating each appointment.  A Missing value falls back to the default value if present. Mandatory, but an empty list is accepted.", position = 1)
    @NotNull
    private List<@Valid AppointmentDetails> appointments;

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
