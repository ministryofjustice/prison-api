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

@ApiModel(description = "Incident Reponses")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"questionnaireQueId", "questionnaireAnsId"})
@Data
public class IncidentResponse implements Comparable<IncidentResponse> {

    @ApiModelProperty(required = true, value = "The Question", example = "Was it a violent assault?")
    private String question;
    @ApiModelProperty(required = true, value = "The Answer to the Question", example = "YES", position = 1)
    private String answer;
    @ApiModelProperty(required = true, value = "Sequence of presented Questions", example = "2131231", position = 2)
    private int questionSeq;
    @ApiModelProperty(required = true, value = "ID for Questionnaire Question", example = "983431", position = 3)
    private Long questionnaireQueId;
    @ApiModelProperty(required = true, value = "ID for Questionnaire Answer", example = "983434", position = 4)
    private Long questionnaireAnsId;
    @ApiModelProperty(required = false, value = "Date response was recorded", example = "2018-03-04T11:00:05", position = 5)
    private LocalDateTime responseDate;
    @ApiModelProperty(required = false, value = "Additional comments for the response to the question", example = "The knife was in his pocket", position = 6)
    private String responseCommentText;
    @ApiModelProperty(required = false, value = "Staff Id recording comment", example = "123123", position = 7)
    private Long recordStaffId;

    @Override
    public int compareTo(final IncidentResponse response) {
        return new CompareToBuilder()
                .append(this.getQuestionSeq(), response.getQuestionSeq())
                .toComparison();
    }
}
