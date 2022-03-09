package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "A type of offence that can be made as part of an adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationOffence {

    @Schema(description = "Offence Id", example = "8")
    private String id;
    @Schema(description = "Offence Code", example = "51:7")
    private String code;
    @Schema(description = "Offence Description", example = "Escapes or absconds from prison or from legal custody")
    private String description;
}
