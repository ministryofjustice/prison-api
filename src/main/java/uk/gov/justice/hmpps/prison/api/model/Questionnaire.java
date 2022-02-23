package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.SortedSet;

@Schema(description = "Questionnaire")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Questionnaire {
    @Schema(required = true, description = "Code to identify this questionnaire", example = "ASSAULTS")
    @NotBlank
    private String code;
    @Schema(required = true, description = "ID internal of this Questionnaire", example = "123412")
    @NotBlank
    private Long questionnaireId;

    @Schema(required = true, description = "List of Questions (with Answers) for this Questionnaire")
    private SortedSet<QuestionnaireQuestion> questions;
}
