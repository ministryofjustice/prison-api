package net.syscon.elite.events.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderDeletionEvent {
    private String offenderIdDisplay;
}

