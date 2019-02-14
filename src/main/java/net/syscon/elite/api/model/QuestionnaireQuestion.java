package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.LocalDateTime;
import java.util.SortedSet;


@ApiModel(description = "Questionnaire Question")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "questionnaireQueId" })
@Data
@ToString
public class QuestionnaireQuestion implements Comparable<QuestionnaireQuestion>{

    private Long questionnaireQueId;
    private int questionSeq;
    private String questionDesc;
    private int questionListSeq;
    private Boolean questionActiveFlag;
    private LocalDateTime questionExpiryDate;
    private Boolean multipleAnswerFlag;
    private Long nextQuestionnaireQueId;

    private SortedSet<QuestionnaireAnswer> answers;

    @Override
    public int compareTo(QuestionnaireQuestion question) {
        return new CompareToBuilder()
                .append(this.getQuestionSeq(), question.getQuestionSeq())
                .toComparison();
    }
}
