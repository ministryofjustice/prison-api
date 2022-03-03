package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Scheduled appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScheduledAppointmentDto {
    @Schema(description = "Appointment id")
    private Long id;

    @Schema(description = "Offender number (e.g. NOMS Number)")
    private String offenderNo;

    @Schema(description = "Offender first name")
    private String firstName;

    @Schema(description = "Offender last name")
    private String lastName;

    @Schema(description = "Date the appointment is scheduled")
    private LocalDate date;

    @Schema(description = "Date and time at which appointment starts")
    private LocalDateTime startTime;

    @Schema(description = "Date and time at which appointment ends")
    private LocalDateTime endTime;

    @Schema(description = "Description of appointment type")
    private String appointmentTypeDescription;

    @Schema(description = "Appointment code")
    private String appointmentTypeCode;

    @Schema(description = "Description of location the appointment is held")
    private String locationDescription;

    @Schema(description = "Id of location the appointment is held")
    private Long locationId;

    @Schema(description = "Staff member who created the appointment")
    private String createUserId;

    @Schema(description = "Agency the appointment belongs to")
    private String agencyId;
}


