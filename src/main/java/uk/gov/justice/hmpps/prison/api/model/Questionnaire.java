package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.SortedSet;

@ApiModel(description = "Questionnaire")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Questionnaire {
    @ApiModelProperty(required = true, value = "Code to identify this questionnaire", example = "ASSAULTS")
    @NotBlank
    private String code;
    @ApiModelProperty(required = true, value = "ID internal of this Questionnaire", example = "123412", position = 1)
    @NotBlank
    private Long questionnaireId;

    @ApiModelProperty(required = true, value = "List of Questions (with Answers) for this Questionnaire", position = 2)
    private SortedSet<QuestionnaireQuestion> questions;
}
