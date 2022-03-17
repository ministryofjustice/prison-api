package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@SuppressWarnings("unused")
@Schema(description = "AssessmentRating")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AssessmentClassification {

    @NotBlank
    @Schema(description = "Offender number (e.g. NOMS Number).", example = "GV09876N")
    private String offenderNo;

    @Schema(description = "The current classification code. This will not have a value if np assessment has been completed", example = "STANDARD")
    private String classificationCode;

    @Schema(description = "The date that the current classification was made. This will not have a value if np assessment has been completed", example = "2018-02-11")
    private LocalDate classificationDate;
}
