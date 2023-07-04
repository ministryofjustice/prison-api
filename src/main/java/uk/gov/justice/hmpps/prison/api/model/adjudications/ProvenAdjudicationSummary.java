package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;


@Schema(description = "Proven Adjudication Summary for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProvenAdjudicationSummary {

    @Schema(requiredMode = REQUIRED, description = "Offender Booking Id")
    private Long bookingId;

    @Schema(requiredMode = REQUIRED, description = "Number of proven adjudications")
    private Integer provenAdjudicationCount;

}
