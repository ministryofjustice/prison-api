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

@Schema(description = "Questionnaire Answer")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"questionnaireAnsId"})
@Data
public class QuestionnaireAnswer implements Comparable<QuestionnaireAnswer> {

    @Schema(required = true, description = "ID for this Answer")
    private Long questionnaireAnsId;
    @Schema(required = true, description = "", example = "1")
    private int answerSeq;
    @Schema(required = true, description = "", example = "YES")
    private String answerDesc;
    @Schema(required = true, description = "", example = "1")
    private int answerListSeq;
    @Schema(required = true, description = "", example = "true")
    private Boolean answerActiveFlag;
    @Schema(required = true, description = "", example = "2017-01-02T00:00:00")
    private LocalDateTime answerExpiryDate;
    @Schema(required = true, description = "", example = "false")
    private Boolean dateRequiredFlag;
    @Schema(required = true, description = "", example = "false")
    private Boolean commentRequiredFlag;

    @Override
    public int compareTo(final QuestionnaireAnswer answer) {
        return new CompareToBuilder()
                .append(this.getAnswerSeq(), answer.getAnswerSeq())
                .toComparison();
    }
}
