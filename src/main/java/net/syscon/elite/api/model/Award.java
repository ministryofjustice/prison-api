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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Adjudication award / sanction
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Adjudication award / sanction")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Award {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String sanctionCode;

    private String sanctionCodeDescription;

    private Integer months;

    private Integer days;

    private BigDecimal limit;

    private String comment;

    @NotNull
    private LocalDate effectiveDate;

    private String status;

    private String statusDescription;

    @NotNull
    private Long hearingId;

    @NotNull
    private Integer hearingSequence;

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
      * Type of award
      */
    @ApiModelProperty(required = true, value = "Type of award")
    @JsonProperty("sanctionCode")
    public String getSanctionCode() {
        return sanctionCode;
    }

    public void setSanctionCode(final String sanctionCode) {
        this.sanctionCode = sanctionCode;
    }

    /**
      * Award type description
      */
    @ApiModelProperty(value = "Award type description")
    @JsonProperty("sanctionCodeDescription")
    public String getSanctionCodeDescription() {
        return sanctionCodeDescription;
    }

    public void setSanctionCodeDescription(final String sanctionCodeDescription) {
        this.sanctionCodeDescription = sanctionCodeDescription;
    }

    /**
      * Number of months duration
      */
    @ApiModelProperty(value = "Number of months duration")
    @JsonProperty("months")
    public Integer getMonths() {
        return months;
    }

    public void setMonths(final Integer months) {
        this.months = months;
    }

    /**
      * Number of days duration
      */
    @ApiModelProperty(value = "Number of days duration")
    @JsonProperty("days")
    public Integer getDays() {
        return days;
    }

    public void setDays(final Integer days) {
        this.days = days;
    }

    /**
      * Compensation amount
      */
    @ApiModelProperty(value = "Compensation amount")
    @JsonProperty("limit")
    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(final BigDecimal limit) {
        this.limit = limit;
    }

    /**
      * Optional details
      */
    @ApiModelProperty(value = "Optional details")
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
      * Start of sanction
      */
    @ApiModelProperty(required = true, value = "Start of sanction")
    @JsonProperty("effectiveDate")
    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(final LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
      * Award status (ref domain OIC_SANCT_ST)
      */
    @ApiModelProperty(value = "Award status (ref domain OIC_SANCT_ST)")
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    /**
      * Award status description
      */
    @ApiModelProperty(value = "Award status description")
    @JsonProperty("statusDescription")
    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }

    /**
      * Id of hearing
      */
    @ApiModelProperty(required = true, value = "Id of hearing")
    @JsonProperty("hearingId")
    public Long getHearingId() {
        return hearingId;
    }

    public void setHearingId(final Long hearingId) {
        this.hearingId = hearingId;
    }

    /**
      * hearing record sequence number
      */
    @ApiModelProperty(required = true, value = "hearing record sequence number")
    @JsonProperty("hearingSequence")
    public Integer getHearingSequence() {
        return hearingSequence;
    }

    public void setHearingSequence(final Integer hearingSequence) {
        this.hearingSequence = hearingSequence;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class Award {\n");
        
        sb.append("  sanctionCode: ").append(sanctionCode).append("\n");
        sb.append("  sanctionCodeDescription: ").append(sanctionCodeDescription).append("\n");
        sb.append("  months: ").append(months).append("\n");
        sb.append("  days: ").append(days).append("\n");
        sb.append("  limit: ").append(limit).append("\n");
        sb.append("  comment: ").append(comment).append("\n");
        sb.append("  effectiveDate: ").append(effectiveDate).append("\n");
        sb.append("  status: ").append(status).append("\n");
        sb.append("  statusDescription: ").append(statusDescription).append("\n");
        sb.append("  hearingId: ").append(hearingId).append("\n");
        sb.append("  hearingSequence: ").append(hearingSequence).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
