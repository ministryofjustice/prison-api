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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * User Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "User Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long staffId;

    @NotBlank
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private Long thumbnailId;

    private String activeCaseLoadId;

    @NotBlank
    private String accountStatus;

    @NotNull
    private LocalDateTime lockDate;

    @NotNull
    private LocalDateTime expiryDate;

    @NotNull
    private boolean lockedFlag;

    @NotNull
    private boolean expiredFlag;

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
      * Staff Id
      */
    @ApiModelProperty(required = true, value = "Staff Id")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    /**
      */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
      */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
      */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
      */
    @ApiModelProperty(value = "")
    @JsonProperty("thumbnailId")
    public Long getThumbnailId() {
        return thumbnailId;
    }

    public void setThumbnailId(Long thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    /**
      */
    @ApiModelProperty(value = "")
    @JsonProperty("activeCaseLoadId")
    public String getActiveCaseLoadId() {
        return activeCaseLoadId;
    }

    public void setActiveCaseLoadId(String activeCaseLoadId) {
        this.activeCaseLoadId = activeCaseLoadId;
    }

    /**
      */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("accountStatus")
    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
      */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("lockDate")
    public LocalDateTime getLockDate() {
        return lockDate;
    }

    public void setLockDate(LocalDateTime lockDate) {
        this.lockDate = lockDate;
    }

    /**
      */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("expiryDate")
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
      */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("lockedFlag")
    public boolean getLockedFlag() {
        return lockedFlag;
    }

    public void setLockedFlag(boolean lockedFlag) {
        this.lockedFlag = lockedFlag;
    }

    /**
      */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("expiredFlag")
    public boolean getExpiredFlag() {
        return expiredFlag;
    }

    public void setExpiredFlag(boolean expiredFlag) {
        this.expiredFlag = expiredFlag;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class UserDetail {\n");
        
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  username: ").append(username).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  thumbnailId: ").append(thumbnailId).append("\n");
        sb.append("  activeCaseLoadId: ").append(activeCaseLoadId).append("\n");
        sb.append("  accountStatus: ").append(accountStatus).append("\n");
        sb.append("  lockDate: ").append(lockDate).append("\n");
        sb.append("  expiryDate: ").append(expiryDate).append("\n");
        sb.append("  lockedFlag: ").append(lockedFlag).append("\n");
        sb.append("  expiredFlag: ").append(expiredFlag).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
