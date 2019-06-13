package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
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
@EqualsAndHashCode
public class Assessment {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    @ApiModelProperty(value = "Booking number.")
    private Long bookingId;

    @NotBlank
    @ApiModelProperty(value = "Offender number (e.g. NOMS Number).")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(value = "Classification code")
    private String classificationCode;

    @NotBlank
    @ApiModelProperty(value = "Classification description")
    private String classification;

    @NotBlank
    @ApiModelProperty(value = "Identifies the type of assessent", example = "CATEGORY")
    private String assessmentCode;

    @NotBlank
    @ApiModelProperty(value = "Assessment description")
    private String assessmentDescription;

    @NotNull
    @ApiModelProperty(value = "Indicates the presence of a cell sharing alert")
    private boolean cellSharingAlertFlag;

    @NotNull
    @ApiModelProperty(value = "Date assessment was created")
    private LocalDate assessmentDate;

    @NotNull
    @ApiModelProperty(value = "Date of next review")
    private LocalDate nextReviewDate;

    @ApiModelProperty(value = "Date of assessment approval")
    private LocalDate approvalDate;

    @ApiModelProperty(value = "Agency id for the assessment approval", example = "MDI")
    private String approvalAgencyId;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Offender Booking Id
      */
    @ApiModelProperty(required = true, value = "Offender Booking Id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Offender Number
      */
    @ApiModelProperty(required = true, value = "Offender Number")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(final String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * Classification code
      */
    @ApiModelProperty(required = true, value = "Classification code")
    @JsonProperty("classificationCode")
    public String getClassificationCode() {
        return classificationCode;
    }

    public void setClassificationCode(final String classificationCode) {
        this.classificationCode = classificationCode;
    }

    /**
      * Classification description
      */
    @ApiModelProperty(required = true, value = "Classification description")
    @JsonProperty("classification")
    public String getClassification() {
        return classification;
    }

    public void setClassification(final String classification) {
        this.classification = classification;
    }

    /**
      * Assessment Code
      */
    @ApiModelProperty(required = true, value = "Assessment Code")
    @JsonProperty("assessmentCode")
    public String getAssessmentCode() {
        return assessmentCode;
    }

    public void setAssessmentCode(final String assessmentCode) {
        this.assessmentCode = assessmentCode;
    }

    /**
      * Assessment Desc
      */
    @ApiModelProperty(required = true, value = "Assessment Desc")
    @JsonProperty("assessmentDescription")
    public String getAssessmentDescription() {
        return assessmentDescription;
    }

    public void setAssessmentDescription(final String assessmentDescription) {
        this.assessmentDescription = assessmentDescription;
    }

    /**
      * Cell Sharing Alert Flag
      */
    @ApiModelProperty(required = true, value = "Cell Sharing Alert Flag")
    @JsonProperty("cellSharingAlertFlag")
    public boolean getCellSharingAlertFlag() {
        return cellSharingAlertFlag;
    }

    public void setCellSharingAlertFlag(final boolean cellSharingAlertFlag) {
        this.cellSharingAlertFlag = cellSharingAlertFlag;
    }

    /**
      * Assessment Date
      */
    @ApiModelProperty(required = true, value = "Assessment Date")
    @JsonProperty("assessmentDate")
    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(final LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    /**
      * Next Review Date
      */
    @ApiModelProperty(required = true, value = "Next Review Date")
    @JsonProperty("nextReviewDate")
    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(final LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class Assessment {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  classificationCode: ").append(classificationCode).append("\n");
        sb.append("  classification: ").append(classification).append("\n");
        sb.append("  assessmentCode: ").append(assessmentCode).append("\n");
        sb.append("  assessmentDescription: ").append(assessmentDescription).append("\n");
        sb.append("  cellSharingAlertFlag: ").append(cellSharingAlertFlag).append("\n");
        sb.append("  assessmentDate: ").append(assessmentDate).append("\n");
        sb.append("  nextReviewDate: ").append(nextReviewDate).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
