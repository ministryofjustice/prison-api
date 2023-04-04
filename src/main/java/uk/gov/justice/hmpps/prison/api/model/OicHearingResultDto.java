package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dto for entity OicHearingResult")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class OicHearingResultDto {

    @Schema(required = true, description = "Nomis oic hearing id.")
    @NotNull
    private Long oicHearingId;

    @Schema(required = true, description = "Sequential number for hearing results.")
    @NotNull
    private Long resultSeq;

    @Schema(required = true, description = "Nomis adjudication id.")
    @NotNull
    private Long agencyIncidentId;

    @Schema(required = true, description = "Sequential number for charge.")
    @NotNull
    private Long chargeSeq;

    @Schema(required = true, description = "The offender\"s plea code on this charge.")
    @NotNull
    private String pleaFindingCode;

    @Schema(required = true, description = "Finding code")
    @NotNull
    private String findingCode;

    @Schema(required = true, description = "Nomis offence id.")
    @NotNull
    private Long oicOffenceId;
}
