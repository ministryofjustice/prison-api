package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@SuppressWarnings("unused")
@ApiModel(description = "AssessmentRating")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AssessmentClassification {

    @NotBlank
    @ApiModelProperty(value = "Offender number (e.g. NOMS Number).", position = 1, example = "GV09876N")
    private String offenderNo;

    @ApiModelProperty(value = "The current classification code. This will not have a value if np assessment has been completed", position = 2, example = "STANDARD")
    private String classificationCode;

    @ApiModelProperty(value = "The date that the current classification was made. This will not have a value if np assessment has been completed", position = 3, example = "2018-02-11")
    private LocalDate classificationDate;
}
