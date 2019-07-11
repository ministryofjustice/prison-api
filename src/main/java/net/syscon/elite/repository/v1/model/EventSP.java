package net.syscon.elite.repository.v1.model;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventSP {
    private Long apiEventId;
    private LocalDateTime eventTimestamp;
    private String agyLocId;
    private String nomsId;
    private String eventType;
    private String eventData_1;
    private String eventData_2;
    private String eventData_3;
}