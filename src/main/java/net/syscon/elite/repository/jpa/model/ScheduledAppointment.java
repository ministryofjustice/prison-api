package net.syscon.elite.repository.jpa.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScheduledAppointment implements Serializable {
    private String offenderNo;
    private LocalDate eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String firstName;
    private String lastName;
    private String appointmentTypeDescription;
    private String appointmentTypeCode;
    private String locationDescription;
    private Long locationId;
    private String auditUserId;
}

