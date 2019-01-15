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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Incentive &amp; Earned Privilege Summary
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Incentive & Earned Privilege Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PrivilegeSummary {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotNull
    private LocalDate iepDate;

    private LocalDateTime iepTime;

    @NotBlank
    private String iepLevel;

    @NotNull
    private Integer daysSinceReview;

    private List<PrivilegeDetail> iepDetails;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Offender booking identifier.
      */
    @ApiModelProperty(required = true, value = "Offender booking identifier.")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Effective date of current IEP level.
      */
    @ApiModelProperty(required = true, value = "Effective date of current IEP level.")
    @JsonProperty("iepDate")
    public LocalDate getIepDate() {
        return iepDate;
    }

    public void setIepDate(LocalDate iepDate) {
        this.iepDate = iepDate;
    }

    /**
      * Effective date & time of current IEP level.
      */
    @ApiModelProperty(value = "Effective date & time of current IEP level.")
    @JsonProperty("iepTime")
    public LocalDateTime getIepTime() {
        return iepTime;
    }

    public void setIepTime(LocalDateTime iepTime) {
        this.iepTime = iepTime;
    }

    /**
      * The current IEP level (e.g. Basic, Standard or Enhanced).
      */
    @ApiModelProperty(required = true, value = "The current IEP level (e.g. Basic, Standard or Enhanced).")
    @JsonProperty("iepLevel")
    public String getIepLevel() {
        return iepLevel;
    }

    public void setIepLevel(String iepLevel) {
        this.iepLevel = iepLevel;
    }

    /**
      * The number of days since last review.
      */
    @ApiModelProperty(required = true, value = "The number of days since last review.")
    @JsonProperty("daysSinceReview")
    public Integer getDaysSinceReview() {
        return daysSinceReview;
    }

    public void setDaysSinceReview(Integer daysSinceReview) {
        this.daysSinceReview = daysSinceReview;
    }

    /**
      * All IEP detail entries for the offender (most recent first).
      */
    @ApiModelProperty(value = "All IEP detail entries for the offender (most recent first).")
    @JsonProperty("iepDetails")
    public List<PrivilegeDetail> getIepDetails() {
        return iepDetails;
    }

    public void setIepDetails(List<PrivilegeDetail> iepDetails) {
        this.iepDetails = iepDetails;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class PrivilegeSummary {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  iepDate: ").append(iepDate).append("\n");
        sb.append("  iepTime: ").append(iepTime).append("\n");
        sb.append("  iepLevel: ").append(iepLevel).append("\n");
        sb.append("  daysSinceReview: ").append(daysSinceReview).append("\n");
        sb.append("  iepDetails: ").append(iepDetails).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
