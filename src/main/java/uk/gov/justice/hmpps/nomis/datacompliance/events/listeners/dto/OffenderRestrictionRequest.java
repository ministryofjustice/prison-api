package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Set;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OffenderRestrictionRequest {

    @JsonProperty("offenderIdDisplay")
    private String offenderIdDisplay;

    @JsonProperty("retentionCheckId")
    private Long retentionCheckId;

    @JsonProperty("restrictionCodes")
    @Singular
    private Set<String> restrictionCodes;

    @JsonProperty("regex")
    private String regex;
}

