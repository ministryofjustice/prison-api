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
    @Schema(description = "The term duration - years", example = "1")
    private Integer years;

    @Schema(description = "The term duration - months", example = "2")
    private Integer months;

    @Schema(description = "The term duration - weeks", example = "3")
    private Integer weeks;

    @Schema(description = "The term duration - days", example = "4")
    private Integer days;

    @Schema(description = "The sentence term code, indicating if this is the term of imprisonment or license")
    private String code;
}
