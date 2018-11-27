package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Incentive &amp; Earned Privilege Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Incentive & Earned Privilege Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PrivilegeDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotNull
    private LocalDate iepDate;

    private LocalDateTime iepTime;

    @NotBlank
    private String agencyId;

    @NotBlank
    private String iepLevel;

    private String comments;

    private String userId;

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
      * Effective date of IEP level.
      */
    @ApiModelProperty(required = true, value = "Effective date of IEP level.")
    @JsonProperty("iepDate")
    public LocalDate getIepDate() {
        return iepDate;
    }

    public void setIepDate(LocalDate iepDate) {
        this.iepDate = iepDate;
    }

    /**
      * Effective date & time of IEP level.
      */
    @ApiModelProperty(value = "Effective date & time of IEP level.")
    @JsonProperty("iepTime")
    public LocalDateTime getIepTime() {
        return iepTime;
    }

    public void setIepTime(LocalDateTime iepTime) {
        this.iepTime = iepTime;
    }

    /**
      * Identifier of Agency this privilege entry is associated with.
      */
    @ApiModelProperty(required = true, value = "Identifier of Agency this privilege entry is associated with.")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    /**
      * The IEP level (e.g. Basic, Standard or Enhanced).
      */
    @ApiModelProperty(required = true, value = "The IEP level (e.g. Basic, Standard or Enhanced).")
    @JsonProperty("iepLevel")
    public String getIepLevel() {
        return iepLevel;
    }

    public void setIepLevel(String iepLevel) {
        this.iepLevel = iepLevel;
    }

    /**
      * Further details relating to this privilege entry.
      */
    @ApiModelProperty(value = "Further details relating to this privilege entry.")
    @JsonProperty("comments")
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
      * Identifier of user related to this privilege entry.
      */
    @ApiModelProperty(value = "Identifier of user related to this privilege entry.")
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class PrivilegeDetail {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  iepDate: ").append(iepDate).append("\n");
        sb.append("  iepTime: ").append(iepTime).append("\n");
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  iepLevel: ").append(iepLevel).append("\n");
        sb.append("  comments: ").append(comments).append("\n");
        sb.append("  userId: ").append(userId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
