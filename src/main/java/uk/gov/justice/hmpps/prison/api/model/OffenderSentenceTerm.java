package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Offender sentence term")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceTerm {
    @Schema(description = "The term duration - years")
    private Integer years;

    @Schema(description = "The term duration - months")
    private Integer months;

    @Schema(description = "The term duration - weeks")
    private Integer weeks;

    @Schema(description = "The term duration - days")
    private Integer days;
}
