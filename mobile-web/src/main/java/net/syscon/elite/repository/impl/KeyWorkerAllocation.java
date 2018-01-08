package net.syscon.elite.repository.impl;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Setter
public class KeyWorkerAllocation {
    private Long bookingId;
    private String agencyId;
    private String reason;
    private String type;
    private Long staffId;
    private LocalDateTime assigned;
    private LocalDateTime expiry;
    private String active;
    private Map<String, Object> additionalProperties;
}
