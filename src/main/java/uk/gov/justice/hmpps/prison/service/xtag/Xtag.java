package uk.gov.justice.hmpps.prison.service.xtag;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Xtag {
    private LocalDateTime nomisTimestamp;
    private XtagContent content;
    private String eventType;
}


