package net.syscon.elite.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScheduledAppointmentDto {
    private String offenderNo;
    private String firstName;
    private String lastName;
    private LocalDate eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String appointmentTypeDescription;
    private String appointmentTypeCode;
    private String locationDescription;
    private Long locationId;
    private String auditUserId;
    private String agencyId;
}


