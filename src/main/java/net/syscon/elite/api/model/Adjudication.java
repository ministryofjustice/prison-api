package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Adjudication {

    private long adjudicationNumber;
    private LocalDateTime reportTime;
    private long agencyIncidentId;
    private String agencyId;
    private long partySeq;
    @Singular private List<AdjudicationCharge> adjudicationCharges;
}
