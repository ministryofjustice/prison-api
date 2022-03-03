package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "An overview of an adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Adjudication {

    @Schema(description = "Adjudication Number", example = "1234567")
    private long adjudicationNumber;
    @Schema(description = "Report Time", example = "2017-03-17T08:02:00")
    private LocalDateTime reportTime;
    @Schema(description = "Agency Incident Id", example = "1484302")
    private long agencyIncidentId;
    @Schema(description = "Agency Id", example = "MDI")
    private String agencyId;
    @Schema(description = "Party Sequence", example = "1")
    private long partySeq;
    @Schema(description = "Charges made as part of the adjudication")
    @Singular
    private List<AdjudicationCharge> adjudicationCharges;
}
