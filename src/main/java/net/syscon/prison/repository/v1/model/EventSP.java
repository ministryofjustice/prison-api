package net.syscon.prison.repository.v1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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