package uk.gov.justice.hmpps.prison.service.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class AdjudicationChargeDto {

    private long adjudicationNumber;
    private LocalDateTime reportTime;
    private long agencyIncidentId;
    private String agencyId;
    private String offenceDescription;
    private String offenceCode;
    private String oicChargeId;
    private long partySeq;
    private String findingCode;
    private LocalDateTime sortTime;
}
