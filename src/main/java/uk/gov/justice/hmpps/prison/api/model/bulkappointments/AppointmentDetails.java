package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "Detail for creating an appointment for a particular bookingId where values should differ from the defaults")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AppointmentDetails {

    @Schema(required = true, description = "The Booking id of the offender for whom the appointment is to be created.", example = "123456")
    @NotNull
    private Long bookingId;

    @Schema(description = "A replacement for the default startTime. ISO 8601 date-time format.  This value, when present, must be in the future.", example = "2018-12-31T23:50")
    @Future
    private LocalDateTime startTime;

    @Schema(description = "A replacement for the default endTime. ISO 8601 date-time format.  This value, when present, must be later than the default startTime, or the startTime in this object if it is defined.", example = "2018-12-31T23:59")
    private LocalDateTime endTime;

    @Schema(description = "The Appointment's details. When present this value replaces the default comment.", example = "Please provide helpful supporting text relevant to this particular appointment when the default comment is not suitable.")
    @Size(max = 4000)
    private String comment;
}
