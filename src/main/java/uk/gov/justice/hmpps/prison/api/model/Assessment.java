package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Map;

/**
 * Assessment
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Assessment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Assessment {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    @ApiModelProperty(value = "Booking number", position = 1, example = "123456")
    private Long bookingId;

    @NotBlank
    @ApiModelProperty(value = "Offender number (e.g. NOMS Number).", position = 2, example = "GV09876N")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(value = "Classification code", position = 3, example = "C")
    private String classificationCode;

    @NotBlank
    @ApiModelProperty(value = "Classification description", position = 4, example = "Cat C")
    private String classification;

    @NotBlank
    @ApiModelProperty(value = "Identifies the type of assessment", position = 5, example = "CATEGORY")
    private String assessmentCode;

    @NotBlank
    @ApiModelProperty(value = "Assessment description", position = 6, example = "Categorisation")
    private String assessmentDescription;

    @NotNull
    @ApiModelProperty(value = "Indicates the presence of a cell sharing alert", position = 7)
    private boolean cellSharingAlertFlag;

    @NotNull
    @ApiModelProperty(value = "Date assessment was created", position = 8, example = "2018-02-11")
    private LocalDate assessmentDate;

    @NotNull
    @ApiModelProperty(value = "Date of next review", position = 9, example = "2018-02-11")
    private LocalDate nextReviewDate;

    @ApiModelProperty(value = "Date of assessment approval", position = 10, example = "2018-02-11")
    private LocalDate approvalDate;

    @ApiModelProperty(value = "The assessment creation agency id", position = 11, example = "MDI")
    private String assessmentAgencyId;

    @ApiModelProperty(value = "The status of the assessment", position = 12, example = "A", allowableValues = "P,A,I")
    private String assessmentStatus;

    @ApiModelProperty(value = "Sequence number of assessment within booking", position = 13, example = "1")
    private Integer assessmentSeq;

    @ApiModelProperty(value = "Comment from assessor", position = 14, example = "Comment details")
    private String assessmentComment;

    @ApiModelProperty(value = "Staff member who made the assessment", position = 15, example = "130000")
    private Long assessorId;

    @ApiModelProperty(value = "Username who made the assessment", position = 16, example = "NGK33Y")
    private String assessorUser;
}
