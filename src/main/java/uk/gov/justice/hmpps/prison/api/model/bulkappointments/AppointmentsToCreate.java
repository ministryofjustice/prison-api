package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Details for creating appointments in bulk")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentsToCreate {
    @Schema(required = true, description = "The default values to be applied to each new appointment. An individual appointment may change the startTime, add or change the endTime and provide text for that appointment's comment.")
    @NotNull
    @Valid
    private AppointmentDefaults appointmentDefaults;

    @Schema(required = true, description = "The details for creating each appointment.  A Missing value falls back to the default value if present. Mandatory, but an empty list is accepted.")
    @NotNull
    private List<@Valid AppointmentDetails> appointments;

    @Schema(description = "If present specifies the number of times to repeat the appointments and the period of the repeat")
    @Valid
    private Repeat repeat;

    public List<AppointmentDetails> withDefaults() {
        return appointments.stream().map(appt -> {
                    var builder = appt.toBuilder();
                    if (appt.getStartTime() == null) builder.startTime(appointmentDefaults.getStartTime());
                    if (appt.getEndTime() == null) builder.endTime(appointmentDefaults.getEndTime());
                    if (appt.getComment() == null) builder.comment(appointmentDefaults.getComment());
                    return builder.build();
                }
        ).toList();
    }

    public boolean moreThanOneOffender() {
        return appointments != null && appointments.size() > 1;
    }
}
