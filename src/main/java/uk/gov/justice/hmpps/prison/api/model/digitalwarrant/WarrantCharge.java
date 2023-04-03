package uk.gov.justice.hmpps.prison.api.model.digitalwarrant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDate;

@Schema(description = "A new offence from a digital warrant")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@With
public class WarrantCharge {
    @Schema(description = "The id of the charge")
    private Long chargeId;

    @Schema(description = "The offence code of the office in the court case")
    private String offenceCode;

    @Schema(description = "The offence statute of the office in the court case")
    private String offenceStatue;

    @Schema(description = "The offence description")
    private String offenceDescription;

    @Schema(description = "The date of the offence")
    private LocalDate offenceDate;

    @Schema(description = "The offence end date")
    private LocalDate offenceEndDate;

    @Schema(description = "Was the verdict guilty or not guilty")
    private boolean guilty;

    @Schema(description = "The id of the court case")
    private Long courtCaseId;

    @Schema(description = "Court case reference")
    private String courtCaseRef;

    @Schema(description = "Court case location")
    private String courtLocation;

    @Schema(description = "The sequence of the sentence from this charge")
    private Integer sentenceSequence;

    @Schema(description = "The sentence date")
    private LocalDate sentenceDate;

    @Schema(description = "The result description of the charge")
    private String resultDescription;

}
