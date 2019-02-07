package net.syscon.elite.api.model.bulkappointments;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
@Builder
public class AppointmentToCreate {
    private Long bookingId;
    private String eventSubType;
    private Date eventDate;
    private Timestamp startTime;
    private Timestamp endTime;
    private String comment;
    private Long locationId;
    private String agencyId;
}
