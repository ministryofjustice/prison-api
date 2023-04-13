package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.FindingCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.PleaFindingCode;


@Schema(description = "Dto for entity OicHearingResult")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class OicHearingResultDto {

    @Schema(description = "The offender's plea code on this charge")
    @NotNull
    private PleaFindingCode pleaFindingCode;

    @Schema( description = "Finding code")
    @NotNull
    private FindingCode findingCode;

}
