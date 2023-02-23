package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@SuppressWarnings("unused")
@Schema(description = "AssessmentQuestion")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssessmentQuestion {

    @NotNull
    @Schema(description = "Question")
    private String question;

    @Schema(description = "The answer given. More than one answer might have been given, in which case the other answers will be in the additionalAnswers property")
    private String answer;

    @Schema(description = "If a question has more than one answer, all but the first answer will be in this property")
    private List<String> additionalAnswers;
}
