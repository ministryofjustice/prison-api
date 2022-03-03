package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Offender Key Worker record representation (to facilitate data migration)
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Key Worker record representation (to facilitate data migration)")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenderKeyWorker {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotBlank
    private String offenderNo;

    @NotNull
    private Long staffId;

    @NotBlank
    private String agencyId;

    @NotNull
    private LocalDateTime assigned;

    private LocalDateTime expired;

    @NotBlank
    private String userId;

    @NotBlank
    private String active;

    @NotNull
    private LocalDateTime created;

    @NotBlank
    private String createdBy;

    private LocalDateTime modified;

    private String modifiedBy;

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
     * The key worker's Staff Id
     */
    @ApiModelProperty(required = true, value = "The key worker's Staff Id")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(final Long staffId) {
        this.staffId = staffId;
    }

    /**
     * Agency Id
     */
    @ApiModelProperty(required = true, value = "Agency Id")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(final String agencyId) {
        this.agencyId = agencyId;
    }

    /**
     * Date and time allocation was assigned
     */
    @ApiModelProperty(required = true, value = "Date and time allocation was assigned")
    @JsonProperty("assigned")
    public LocalDateTime getAssigned() {
        return assigned;
    }

    public void setAssigned(final LocalDateTime assigned) {
        this.assigned = assigned;
    }

    /**
     * Date and time allocation expired
     */
    @ApiModelProperty(value = "Date and time allocation expired")
    @JsonProperty("expired")
    public LocalDateTime getExpired() {
        return expired;
    }

    public void setExpired(final LocalDateTime expired) {
        this.expired = expired;
    }

    /**
     * Username of user who processed allocation
     */
    @ApiModelProperty(required = true, value = "Username of user who processed allocation")
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    /**
     * Y
     */
    @ApiModelProperty(required = true, value = "Y")
    @JsonProperty("active")
    public String getActive() {
        return active;
    }

    public void setActive(final String active) {
        this.active = active;
    }

    /**
     * Date and time allocation record was created
     */
    @ApiModelProperty(required = true, value = "Date and time allocation record was created")
    @JsonProperty("created")
    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    /**
     * Username of user who created allocation record
     */
    @ApiModelProperty(required = true, value = "Username of user who created allocation record")
    @JsonProperty("createdBy")
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Date and time allocation record was last modified
     */
    @ApiModelProperty(value = "Date and time allocation record was last modified")
    @JsonProperty("modified")
    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(final LocalDateTime modified) {
        this.modified = modified;
    }

    /**
     * Username of user who last modified allocation record
     */
    @ApiModelProperty(value = "Username of user who last modified allocation record")
    @JsonProperty("modifiedBy")
    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(final String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class OffenderKeyWorker {\n");

        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  assigned: ").append(assigned).append("\n");
        sb.append("  expired: ").append(expired).append("\n");
        sb.append("  userId: ").append(userId).append("\n");
        sb.append("  active: ").append(active).append("\n");
        sb.append("  created: ").append(created).append("\n");
        sb.append("  createdBy: ").append(createdBy).append("\n");
        sb.append("  modified: ").append(modified).append("\n");
        sb.append("  modifiedBy: ").append(modifiedBy).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
