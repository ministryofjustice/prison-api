package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Cell move result")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalMoveResult {
    @Schema(requiredMode = REQUIRED, description = "Unique, numeric booking id.", example = "1234134")
    private Long bookingId;

    @Schema(requiredMode = REQUIRED, description = "Identifier of agency that offender is associated with.", example = "MDI")
    private String agencyId;

    @Schema(description = "Identifier of living unit (e.g. cell) that offender is assigned to.", example = "123123")
    private Long assignedLivingUnitId;

    @Schema(description = "Description of living unit (e.g. cell) that offender is assigned to.", example = "MDI-1-1-3")
    private String assignedLivingUnitDesc;

    @Schema(description = "Bed assignment sequence associated with the entry created for this cell move ", example = "2")
    private Integer bedAssignmentHistorySequence;
}
