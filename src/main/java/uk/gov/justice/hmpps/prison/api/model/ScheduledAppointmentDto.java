package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ApiModel(description = "Scheduled appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScheduledAppointmentDto {
    @ApiModelProperty(value = "Appointment id")
    private Long id;

    @ApiModelProperty(value = "Offender number (e.g. NOMS Number)")
    private String offenderNo;

    @ApiModelProperty(value = "Offender first name")
    private String firstName;

    @ApiModelProperty(value = "Offender last name")
    private String lastName;

    @ApiModelProperty(value = "Date the appointment is scheduled")
    private LocalDate date;

    @ApiModelProperty(value = "Date and time at which appointment starts")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "Date and time at which appointment ends")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "Description of appointment type")
    private String appointmentTypeDescription;

    @ApiModelProperty(value = "Appointment code")
    private String appointmentTypeCode;

    @ApiModelProperty(value = "Description of location the appointment is held")
    private String locationDescription;

    @ApiModelProperty(value = "Id of location the appointment is held")
    private Long locationId;

    @ApiModelProperty(value = "Staff member who created the appointment")
    private String createUserId;

    @ApiModelProperty(value = "Agency the appointment belongs to")
    private String agencyId;
}


