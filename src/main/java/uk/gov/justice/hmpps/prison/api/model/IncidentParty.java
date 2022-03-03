package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.CompareToBuilder;

@ApiModel(description = "Incident Party")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IncidentParty implements Comparable<IncidentParty> {

    @ApiModelProperty(required = true, value = "Booking Id of offender involved", example = "1241232", position = 0)
    private Long bookingId;
    @ApiModelProperty(required = true, value = "Sequence or each party member", example = "1", position = 1)
    private Long partySeq;
    @ApiModelProperty(required = false, value = "Staff Member ID (optional)", example = "1534133", position = 2)
    private Long staffId;
    @ApiModelProperty(required = false, value = "Person (non-staff) ID (optional)", example = "544233", position = 3)
    private Long personId;
    @ApiModelProperty(required = true, value = "Role in the Incident", example = "ASSIAL", position = 4)
    private String participationRole;
    @ApiModelProperty(required = true, value = "Outcome Code", example = "POR", position = 5)
    private String outcomeCode;
    @ApiModelProperty(required = true, value = "Additional Comments", example = "Some additional Information", position = 6)
    private String commentText;
    @ApiModelProperty(required = true, value = "Incident Case ID", example = "12431243", position = 7)
    private Long incidentCaseId;

    @Override
    public int compareTo(final IncidentParty party) {
        return new CompareToBuilder()
                .append(this.getPartySeq(), party.getPartySeq())
                .toComparison();
    }
}
