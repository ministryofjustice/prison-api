package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;

@Schema(description = "Incident Party")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class IncidentParty implements Comparable<IncidentParty> {

    @Schema(required = true, description = "Booking Id of offender involved", example = "1241232")
    private Long bookingId;
    @Schema(required = true, description = "Sequence or each party member", example = "1")
    private Long partySeq;
    @Schema(description = "Staff Member ID (optional)", example = "1534133")
    private Long staffId;
    @Schema(description = "Person (non-staff) ID (optional)", example = "544233")
    private Long personId;
    @Schema(required = true, description = "Role in the Incident", example = "ASSIAL")
    private String participationRole;
    @Schema(required = true, description = "Outcome Code", example = "POR")
    private String outcomeCode;
    @Schema(required = true, description = "Additional Comments", example = "Some additional Information")
    private String commentText;
    @Schema(required = true, description = "Incident Case ID", example = "12431243")
    private Long incidentCaseId;

    public IncidentParty(Long bookingId, Long partySeq, Long staffId, Long personId, String participationRole, String outcomeCode, String commentText, Long incidentCaseId) {
        this.bookingId = bookingId;
        this.partySeq = partySeq;
        this.staffId = staffId;
        this.personId = personId;
        this.participationRole = participationRole;
        this.outcomeCode = outcomeCode;
        this.commentText = commentText;
        this.incidentCaseId = incidentCaseId;
    }

    public IncidentParty() {
    }

    @Override
    public int compareTo(final IncidentParty party) {
        return new CompareToBuilder()
                .append(this.getPartySeq(), party.getPartySeq())
                .toComparison();
    }
}
