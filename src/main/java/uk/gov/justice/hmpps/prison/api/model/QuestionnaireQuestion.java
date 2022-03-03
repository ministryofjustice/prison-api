package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.LocalDateTime;
import java.util.SortedSet;


@ApiModel(description = "Questionnaire Question")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"questionnaireQueId"})
@Data
public class QuestionnaireQuestion implements Comparable<QuestionnaireQuestion> {

    @ApiModelProperty(required = true, value = "")
    private Long questionnaireQueId;
    @ApiModelProperty(required = true, value = "", position = 1)
    private int questionSeq;
    @ApiModelProperty(required = true, value = "", position = 2)
    private String questionDesc;
    @ApiModelProperty(required = true, value = "", position = 3)
    private int questionListSeq;
    @ApiModelProperty(required = true, value = "", position = 4)
    private Boolean questionActiveFlag;
    @ApiModelProperty(required = true, value = "", position = 5)
    private LocalDateTime questionExpiryDate;
    @ApiModelProperty(required = true, value = "", position = 6)
    private Boolean multipleAnswerFlag;
    @ApiModelProperty(required = true, value = "", position = 7)
    private Long nextQuestionnaireQueId;
    @ApiModelProperty(required = true, value = "", position = 8)
    private SortedSet<QuestionnaireAnswer> answers;

    @Override
    public int compareTo(final QuestionnaireQuestion question) {
        return new CompareToBuilder()
                .append(this.getQuestionSeq(), question.getQuestionSeq())
                .toComparison();
    }
}
