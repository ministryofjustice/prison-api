package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FreeTextCheck {

    @JsonProperty("offenderIdDisplay")
    private String offenderIdDisplay;

    @JsonProperty("retentionCheckId")
    private Long retentionCheckId;

    @JsonProperty("regex")
    private List<String> regex;
}

