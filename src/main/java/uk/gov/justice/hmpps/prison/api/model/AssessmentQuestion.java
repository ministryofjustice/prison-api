package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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

    @ApiModelProperty(value = "Answer", position = 2)
    private String answer;
}
