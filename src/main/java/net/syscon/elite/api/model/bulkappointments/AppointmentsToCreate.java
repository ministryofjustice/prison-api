package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import net.syscon.util.DateTimeConverter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
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

    public List<AppointmentToCreate> flatten(String agencyId) {
        return appointments.stream().map(detail -> {
            LocalDateTime startTime = detail.getStartTime() == null ? appointmentDefaults.getStartTime() : detail.getStartTime();
            LocalDateTime endTime = detail.getEndTime() == null ? appointmentDefaults.getEndTime() : detail.getEndTime();

            return AppointmentToCreate
                    .builder()
                    .bookingId(detail.getBookingId())
                    .eventSubType(appointmentDefaults.getAppointmentType())
                    .eventDate(DateTimeConverter.toDate(startTime.toLocalDate()))
                    .startTime(DateTimeConverter.fromLocalDateTime(startTime))
                    .endTime(DateTimeConverter.fromLocalDateTime(endTime))
                    .comment(detail.getComment() == null ? appointmentDefaults.getComment() : detail.getComment())
                    .locationId(appointmentDefaults.getLocationId())
                    .agencyId(agencyId)
                    .build();
                }
        ).collect(Collectors.toList());
    }
}
