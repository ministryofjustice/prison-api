package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "A charge which was made as part of an adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationCharge {

    @Schema(description = "Charge Id", example = "1506763/1")
    private String oicChargeId;
    @Schema(description = "Offence Code", example = "51:22")
    private String offenceCode;
    @Schema(description = "Offence Description", example = "Disobeys any lawful order")
    private String offenceDescription;
    @Schema(description = "Offence Finding Code", example = "PROVED")
    private String findingCode;
}
