package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.syscon.elite.api.model.ScheduledEvent;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Outcomes from a bulk create appointments request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CreateAppointmentsOutcomes {
    @ApiModelProperty(required=true, value="The set of ScheduledEvents that were created")
    @NotNull
    private List<ScheduledEvent> createdEvents = new ArrayList<>();


    @ApiModelProperty(required=true, value="Error responses for each rejected AppointmentDetails")
    @NotNull
    private List<RejectedAppointment> rejectedAppointments = new ArrayList<>();
}
