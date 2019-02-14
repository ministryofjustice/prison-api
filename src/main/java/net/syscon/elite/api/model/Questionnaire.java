package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.util.SortedSet;

@ApiModel(description = "Questionnaire")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Questionnaire {
    private String code;
    private Long questionnaireId;

    private SortedSet<QuestionnaireQuestion> questions;
}
