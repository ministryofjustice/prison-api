package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "Reasonable Adjustments")
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ReasonableAdjustments {

    @Schema(description = "Reasonable Adjustments")
    List<ReasonableAdjustment> reasonableAdjustments;
}
