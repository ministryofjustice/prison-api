package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@SuppressWarnings("unused")
@ApiModel(description = "AssessmentSummary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class AssessmentSummary {

    @NotNull
    @ApiModelProperty(value = "Booking number", position = 1, example = "123456")
    private Long bookingId;

    @NotNull
    @ApiModelProperty(value = "Sequence number of assessment within booking", position = 2, example = "1")
    private Integer assessmentSeq;

    @NotBlank
    @ApiModelProperty(value = "Offender number (e.g. NOMS Number).", position = 3, example = "GV09876N")
    private String offenderNo;

    @ApiModelProperty(value = "Classification code. This will not have a value if the assessment is incomplete or pending", position = 4, example = "STANDARD")
    private String classificationCode;

    @NotBlank
    @ApiModelProperty(value = "Identifies the type of assessment", position = 5, example = "CSR")
    private String assessmentCode;

    @NotNull
    @ApiModelProperty(value = "Indicates whether this is a CSRA assessment", position = 6)
    private boolean cellSharingAlertFlag;

    @NotNull
    @ApiModelProperty(value = "Date assessment was created", position = 7, example = "2018-02-11")
    private LocalDate assessmentDate;

    @ApiModelProperty(value = "The assessment creation agency id", position = 8, example = "MDI")
    private String assessmentAgencyId;

    @ApiModelProperty(value = "Comment from assessor", position = 9, example = "Comment details")
    private String assessmentComment;

    @ApiModelProperty(value = "Username who made the assessment", position = 8, example = "NGK33Y")
    private String assessorUser;

    @ApiModelProperty(value = "Date of next review", position = 9, example = "2018-02-11")
    private LocalDate nextReviewDate;
}
