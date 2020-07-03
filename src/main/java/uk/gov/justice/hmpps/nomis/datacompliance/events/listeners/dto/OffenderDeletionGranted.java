package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OffenderDeletionGranted {

    @JsonProperty("offenderIdDisplay")
    private String offenderIdDisplay;

    @JsonProperty("referralId")
    private Long referralId;

    @Builder.Default
    @JsonProperty("offenderIds")
    private Set<Long> offenderIds = new HashSet<>();

    @Builder.Default
    @JsonProperty("offenderBookIds")
    private Set<Long> offenderBookIds = new HashSet<>();
}

