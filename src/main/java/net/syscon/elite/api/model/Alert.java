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
 * Alert
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Alert {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long alertId;

    @NotNull
    private Long bookingId;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private String alertType;

    @NotBlank
    private String alertTypeDescription;

    @NotBlank
    private String alertCode;

    @NotBlank
    private String alertCodeDescription;

    @NotBlank
    private String comment;

    @NotNull
    private LocalDate dateCreated;

    private LocalDate dateExpires;

    @NotNull
    private boolean expired;

    @NotNull
    private boolean active;

    private String addedByFirstName;

    private String addedByLastName;

    private String expiredByFirstName;

    private String expiredByLastName;

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
      * Alert Id
      */
    @ApiModelProperty(required = true, value = "Alert Id")
    @JsonProperty("alertId")
    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(final Long alertId) {
        this.alertId = alertId;
    }

    /**
      * Offender booking id.
      */
    @ApiModelProperty(required = true, value = "Offender booking id.")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Offender Unique Reference
      */
    @ApiModelProperty(required = true, value = "Offender Unique Reference")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(final String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * Alert Type
      */
    @ApiModelProperty(required = true, value = "Alert Type")
    @JsonProperty("alertType")
    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(final String alertType) {
        this.alertType = alertType;
    }

    /**
      * Alert Type Description
      */
    @ApiModelProperty(required = true, value = "Alert Type Description")
    @JsonProperty("alertTypeDescription")
    public String getAlertTypeDescription() {
        return alertTypeDescription;
    }

    public void setAlertTypeDescription(final String alertTypeDescription) {
        this.alertTypeDescription = alertTypeDescription;
    }

    /**
      * Alert Code
      */
    @ApiModelProperty(required = true, value = "Alert Code")
    @JsonProperty("alertCode")
    public String getAlertCode() {
        return alertCode;
    }

    public void setAlertCode(final String alertCode) {
        this.alertCode = alertCode;
    }

    /**
      * Alert Code Description
      */
    @ApiModelProperty(required = true, value = "Alert Code Description")
    @JsonProperty("alertCodeDescription")
    public String getAlertCodeDescription() {
        return alertCodeDescription;
    }

    public void setAlertCodeDescription(final String alertCodeDescription) {
        this.alertCodeDescription = alertCodeDescription;
    }

    /**
      * Alert comments
      */
    @ApiModelProperty(required = true, value = "Alert comments")
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
      * Date Alert created
      */
    @ApiModelProperty(required = true, value = "Date Alert created")
    @JsonProperty("dateCreated")
    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(final LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
      * Date the alert expires
      */
    @ApiModelProperty(value = "Date the alert expires")
    @JsonProperty("dateExpires")
    public LocalDate getDateExpires() {
        return dateExpires;
    }

    public void setDateExpires(final LocalDate dateExpires) {
        this.dateExpires = dateExpires;
    }

    /**
      * True / False indicated expired
      */
    @ApiModelProperty(required = true, value = "True / False indicated expired")
    @JsonProperty("expired")
    public boolean getExpired() {
        return expired;
    }

    public void setExpired(final boolean expired) {
        this.expired = expired;
    }

    /**
      * Status is active
      */
    @ApiModelProperty(required = true, value = "Status is active")
    @JsonProperty("active")
    public boolean getActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
      * First name of the user who added the alert
      */
    @ApiModelProperty(value = "First name of the user who added the alert")
    @JsonProperty("addedByFirstName")
    public String getAddedByFirstName() {
        return addedByFirstName;
    }

    public void setAddedByFirstName(final String addedByFirstName) {
        this.addedByFirstName = addedByFirstName;
    }

    /**
      * Last name of the user who added the alert
      */
    @ApiModelProperty(value = "Last name of the user who added the alert")
    @JsonProperty("addedByLastName")
    public String getAddedByLastName() {
        return addedByLastName;
    }

    public void setAddedByLastName(final String addedByLastName) {
        this.addedByLastName = addedByLastName;
    }

    /**
      * First name of the user who expired the alert
      */
    @ApiModelProperty(value = "First name of the user who expired the alert")
    @JsonProperty("expiredByFirstName")
    public String getExpiredByFirstName() {
        return expiredByFirstName;
    }

    public void setExpiredByFirstName(final String expiredByFirstName) {
        this.expiredByFirstName = expiredByFirstName;
    }

    /**
      * Last name of the user who expired the alert
      */
    @ApiModelProperty(value = "Last name of the user who expired the alert")
    @JsonProperty("expiredByLastName")
    public String getExpiredByLastName() {
        return expiredByLastName;
    }

    public void setExpiredByLastName(final String expiredByLastName) {
        this.expiredByLastName = expiredByLastName;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class Alert {\n");
        
        sb.append("  alertId: ").append(alertId).append("\n");
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  alertType: ").append(alertType).append("\n");
        sb.append("  alertTypeDescription: ").append(alertTypeDescription).append("\n");
        sb.append("  alertCode: ").append(alertCode).append("\n");
        sb.append("  alertCodeDescription: ").append(alertCodeDescription).append("\n");
        sb.append("  comment: ").append(comment).append("\n");
        sb.append("  dateCreated: ").append(dateCreated).append("\n");
        sb.append("  dateExpires: ").append(dateExpires).append("\n");
        sb.append("  expired: ").append(expired).append("\n");
        sb.append("  active: ").append(active).append("\n");
        sb.append("  addedByFirstName: ").append(addedByFirstName).append("\n");
        sb.append("  addedByLastName: ").append(addedByLastName).append("\n");
        sb.append("  expiredByFirstName: ").append(expiredByFirstName).append("\n");
        sb.append("  expiredByLastName: ").append(expiredByLastName).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
