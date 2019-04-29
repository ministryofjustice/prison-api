package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel(description = "An overview of an adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Adjudication {

    @ApiModelProperty("Adjudication Number")
    private long adjudicationNumber;
    @ApiModelProperty("Report Time")
    private LocalDateTime reportTime;
    @ApiModelProperty("Agency Incident Id")
    private long agencyIncidentId;
    @ApiModelProperty("Agency Id")
    private String agencyId;
    @ApiModelProperty("Party Sequence")
    private long partySeq;
    @ApiModelProperty("Charges made as part of the adjudication")
    @Singular private List<AdjudicationCharge> adjudicationCharges;
}
