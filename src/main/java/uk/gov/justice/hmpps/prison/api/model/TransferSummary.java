package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@SuppressWarnings("unused")
@Schema(description = "The container object for transfer and movement events")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferSummary {

    @Schema(description = "List of scheduled or completed court events")
    private List<CourtEvent> courtEvents;

    @Schema(description = "List of scheduled or completed offender events")
    private List<TransferEvent> transferEvents;

    @Schema(description = "List of scheduled or completed release events")
    private List<ReleaseEvent> releaseEvents;

    @Schema(description = "List of confirmed movements")
    private List<MovementSummary> movements;
}
