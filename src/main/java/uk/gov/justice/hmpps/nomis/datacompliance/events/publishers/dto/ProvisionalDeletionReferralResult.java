package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;


@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class ProvisionalDeletionReferralResult {

    @JsonProperty("referralId")
    private Long referralId;

    @JsonProperty("offenderIdDisplay")
    private String offenderIdDisplay;

    @JsonProperty("subsequentChangesIdentified")
    private boolean subsequentChangesIdentified;

    @JsonProperty("agencyLocationId")
    private String agencyLocationId;

    @Singular
    @JsonProperty("offenceCodes")
    private Set<String> offenceCodes;

    @Singular
    @JsonProperty("alertCodes")
    private Set<String> alertCodes;


    public static ProvisionalDeletionReferralResult changesIdentifiedResult(final String offenderNumber, final Long referralId){
        return ProvisionalDeletionReferralResult.builder().offenderIdDisplay(offenderNumber).referralId(referralId).subsequentChangesIdentified(true).build();
    }
}

