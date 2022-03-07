package uk.gov.justice.hmpps.prison.api.model.adjudications;

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

    @ApiModelProperty(value = "Adjudication Number", example = "1234567")
    private long adjudicationNumber;
    @ApiModelProperty(value = "Report Time", example = "2017-03-17T08:02:00")
    private LocalDateTime reportTime;
    @ApiModelProperty(value = "Agency Incident Id", example = "1484302")
    private long agencyIncidentId;
    @ApiModelProperty(value = "Agency Id", example = "MDI")
    private String agencyId;
    @ApiModelProperty(value = "Party Sequence", example = "1")
    private long partySeq;
    @ApiModelProperty("Charges made as part of the adjudication")
    @Singular
    private List<AdjudicationCharge> adjudicationCharges;
}
