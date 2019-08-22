package net.syscon.elite.repository.v1.model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UnavailabilityReasonSP {

    private String reason;
    private LocalDate eventDate;
    private String visitId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;

    public String getEventDateAsString() {
        return eventDate.toString();
    }
}
