package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@SuppressWarnings("unused")
@ApiModel(description = "AssessmentQuestion")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssessmentQuestion {

    @NotNull
    @ApiModelProperty(value = "Question", position = 1)
    private String question;

    @ApiModelProperty(value = "The answer given. More than one answer might have been given, in which case the other answers will be in the additionalAnswers property", position = 2)
    private String answer;

    @ApiModelProperty(value = "If a question has more than one answer, all but the first answer will be in this property", position = 3)
    private List<String> additionalAnswers;
}
