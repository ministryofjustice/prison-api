package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "Details for creating appointments in bulk")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class NewAppointments {
    @ApiModelProperty(value="The default values to be applied to each new appointment")
    @NotNull
    private AppointmentDefaults appointmentDefaults;

    @ApiModelProperty(value = "The details for creating each appointment.  A Missing value falls back to the default value if present")
    @NotNull
    private List<AppointmentDetails> appointments;
}
