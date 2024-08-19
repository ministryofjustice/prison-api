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

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;


@Schema(description = "Questionnaire Question")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"questionnaireQueId"})
@Data
public class QuestionnaireQuestion implements Comparable<QuestionnaireQuestion> {

    @Schema(requiredMode = REQUIRED, description = "")
    private Long questionnaireQueId;
    @Schema(requiredMode = REQUIRED, description = "")
    private int questionSeq;
    @Schema(requiredMode = REQUIRED, description = "")
    private String questionDesc;
    @Schema(requiredMode = REQUIRED, description = "")
    private int questionListSeq;
    @Schema(requiredMode = REQUIRED, description = "")
    private Boolean questionActiveFlag;
    @Schema(requiredMode = REQUIRED, description = "")
    private LocalDateTime questionExpiryDate;
    @Schema(requiredMode = REQUIRED, description = "")
    private Boolean multipleAnswerFlag;
    @Schema(requiredMode = REQUIRED, description = "")
    private Long nextQuestionnaireQueId;
    @Schema(requiredMode = REQUIRED, description = "")
    private SortedSet<QuestionnaireAnswer> answers;

    @Override
    public int compareTo(final QuestionnaireQuestion question) {
        return new CompareToBuilder()
                .append(this.getQuestionSeq(), question.getQuestionSeq())
                .toComparison();
    }
}
