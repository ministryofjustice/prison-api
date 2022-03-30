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

@Schema(description = "Incident Reponses")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"questionnaireQueId", "questionnaireAnsId"})
@Data
public class IncidentResponse implements Comparable<IncidentResponse> {

    @Schema(required = true, description = "The Question", example = "Was it a violent assault?")
    private String question;
    @Schema(required = true, description = "The Answer to the Question", example = "YES")
    private String answer;
    @Schema(required = true, description = "Sequence of presented Questions", example = "2131231")
    private int questionSeq;
    @Schema(required = true, description = "ID for Questionnaire Question", example = "983431")
    private Long questionnaireQueId;
    @Schema(required = true, description = "ID for Questionnaire Answer", example = "983434")
    private Long questionnaireAnsId;
    @Schema(description = "Date response was recorded", example = "2018-03-04T11:00:05")
    private LocalDateTime responseDate;
    @Schema(description = "Additional comments for the response to the question", example = "The knife was in his pocket")
    private String responseCommentText;
    @Schema(description = "Staff Id recording comment", example = "123123")
    private Long recordStaffId;

    @Override
    public int compareTo(final IncidentResponse response) {
        return new CompareToBuilder()
                .append(this.getQuestionSeq(), response.getQuestionSeq())
                .toComparison();
    }
}
