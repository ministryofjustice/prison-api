package net.syscon.prison.repository.v1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VisitSlotsSP {

    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private Long maxGroups;
    private Long maxAdults;
    private Long capacity;
    private Long groupsBooked;
    private Long visitorsBooked;
    private Long adultsBooked;
}
