package net.syscon.elite.repository.v1.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VisitSlotsSP {

    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private Long max_groups;
    private Long max_adults;
    private Long capacity;
    private Long groups_booked;
    private Long visitors_booked;
    private Long adults_booked;
}
