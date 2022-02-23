package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.LocalDateTime;
import java.util.SortedSet;


@Schema(description = "Questionnaire Question")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"questionnaireQueId"})
@Data
public class QuestionnaireQuestion implements Comparable<QuestionnaireQuestion> {

    @Schema(required = true, description = "")
    private Long questionnaireQueId;
    @Schema(required = true, description = "")
    private int questionSeq;
    @Schema(required = true, description = "")
    private String questionDesc;
    @Schema(required = true, description = "")
    private int questionListSeq;
    @Schema(required = true, description = "")
    private Boolean questionActiveFlag;
    @Schema(required = true, description = "")
    private LocalDateTime questionExpiryDate;
    @Schema(required = true, description = "")
    private Boolean multipleAnswerFlag;
    @Schema(required = true, description = "")
    private Long nextQuestionnaireQueId;
    @Schema(required = true, description = "")
    private SortedSet<QuestionnaireAnswer> answers;

    @Override
    public int compareTo(final QuestionnaireQuestion question) {
        return new CompareToBuilder()
                .append(this.getQuestionSeq(), question.getQuestionSeq())
                .toComparison();
    }
}
