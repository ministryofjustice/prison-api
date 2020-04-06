package uk.gov.justice.hmpps.nomis.datacompliance.events.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderDeletionCompleteEvent {
    private String offenderIdDisplay;
}

