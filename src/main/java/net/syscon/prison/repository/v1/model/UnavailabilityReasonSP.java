package net.syscon.prison.repository.v1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    private Long visitId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;

    public String getEventDateAsString() {
        return eventDate.toString();
    }
}
