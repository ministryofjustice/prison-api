package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.syscon.elite.api.model.ScheduledEvent;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiModel(description = "Outcomes from a bulk create appointments request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CreateAppointmentsOutcomes {
    @ApiModelProperty(required=true, value="The set of ScheduledEvents that were created")
    @NotNull
    private final List<ScheduledEvent> createdEvents;


    @ApiModelProperty(required=true, value="Error responses for each rejected AppointmentDetails")
    @NotNull
    private final List<RejectedAppointment> rejectedAppointments;

    private CreateAppointmentsOutcomes(List<ScheduledEvent> createdEvents, List<RejectedAppointment> rejectedAppointments) {
        this.createdEvents = createdEvents;
        this.rejectedAppointments = rejectedAppointments;
    }

    public static CreateAppointmentsOutcomes success(ScheduledEvent e) {
        return new CreateAppointmentsOutcomes(Collections.singletonList(e), Collections.emptyList());
    }

    public static CreateAppointmentsOutcomes failure(RejectedAppointment a) {
        return new CreateAppointmentsOutcomes(Collections.emptyList(), Collections.singletonList(a));
    }

    public static CreateAppointmentsOutcomes accumulator() {
        return new CreateAppointmentsOutcomes(new ArrayList<>(), new ArrayList<>());
    }

    public void add(CreateAppointmentsOutcomes that) {
        createdEvents.addAll(that.getCreatedEvents());
        rejectedAppointments.addAll(that.getRejectedAppointments());
    }
}
