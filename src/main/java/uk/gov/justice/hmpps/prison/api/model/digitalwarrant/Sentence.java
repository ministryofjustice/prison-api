package uk.gov.justice.hmpps.prison.api.model.digitalwarrant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "A new sentence from a digital warrant")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sentence {

    @Schema(description = "The type of sentence")
    private String sentenceType;

    @Schema(description = "The category of sentence")
    private String sentenceCategory;

    @Schema(description = "The date of sentencing")
    private LocalDate sentenceDate;

    @Schema(description = "Days sentence to")
    private Integer days;

    @Schema(description = "Weeks sentence to")
    private Integer weeks;

    @Schema(description = "Months sentence to")
    private Integer months;

    @Schema(description = "Years sentence to")
    private Integer years;

    @Schema(description = "The id of the offender charge")
    private Long offenderChargeId;

    @Schema(description = "The id of the court case")
    private Long courtCaseId;
}
