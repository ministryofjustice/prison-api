package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.LocalDateTime;

@ApiModel(description = "Questionnaire Answer")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "questionnaireAnsId" })
@Data
@ToString
public class QuestionnaireAnswer implements Comparable<QuestionnaireAnswer>{

    private Long questionnaireAnsId;
    private int answerSeq;
    private String answerDesc;
    private int answerListSeq;
    private Boolean answerActiveFlag;
    private LocalDateTime answerExpiryDate;
    private Boolean dateRequiredFlag;
    private Boolean commentRequiredFlag;

    @Override
    public int compareTo(QuestionnaireAnswer answer) {
        return new CompareToBuilder()
                .append(this.getAnswerSeq(), answer.getAnswerSeq())
                .toComparison();
    }
}
