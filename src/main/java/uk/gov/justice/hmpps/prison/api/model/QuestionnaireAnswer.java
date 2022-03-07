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

@ApiModel(description = "Questionnaire Answer")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"questionnaireAnsId"})
@Data
public class QuestionnaireAnswer implements Comparable<QuestionnaireAnswer> {

    @ApiModelProperty(required = true, value = "ID for this Answer")
    private Long questionnaireAnsId;
    @ApiModelProperty(required = true, value = "", example = "1", position = 1)
    private int answerSeq;
    @ApiModelProperty(required = true, value = "", example = "YES", position = 2)
    private String answerDesc;
    @ApiModelProperty(required = true, value = "", example = "1", position = 3)
    private int answerListSeq;
    @ApiModelProperty(required = true, value = "", example = "true", position = 4)
    private Boolean answerActiveFlag;
    @ApiModelProperty(required = true, value = "", example = "2017-01-02T00:00:00", position = 5)
    private LocalDateTime answerExpiryDate;
    @ApiModelProperty(required = true, value = "", example = "false", position = 6)
    private Boolean dateRequiredFlag;
    @ApiModelProperty(required = true, value = "", example = "false", position = 7)
    private Boolean commentRequiredFlag;

    @Override
    public int compareTo(final QuestionnaireAnswer answer) {
        return new CompareToBuilder()
                .append(this.getAnswerSeq(), answer.getAnswerSeq())
                .toComparison();
    }
}
