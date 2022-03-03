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
import java.util.HashMap;
import java.util.Map;

/**
 * Staff User Role
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Staff User Role")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StaffUserRole {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private Long roleId;

    @NotBlank
    private String roleCode;

    @NotBlank
    private String roleName;

    private String parentRoleCode;

    private String caseloadId;

    @NotBlank
    private String username;

    @NotNull
    private Long staffId;

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
     * Role Id
     */
    @ApiModelProperty(required = true, value = "Role Id")
    @JsonProperty("roleId")
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(final Long roleId) {
        this.roleId = roleId;
    }

    /**
     * code for this role
     */
    @ApiModelProperty(required = true, value = "code for this role")
    @JsonProperty("roleCode")
    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(final String roleCode) {
        this.roleCode = roleCode;
    }

    /**
     * Full text description of the role type
     */
    @ApiModelProperty(required = true, value = "Full text description of the role type")
    @JsonProperty("roleName")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    /**
     * role code of the parent role
     */
    @ApiModelProperty(value = "role code of the parent role")
    @JsonProperty("parentRoleCode")
    public String getParentRoleCode() {
        return parentRoleCode;
    }

    public void setParentRoleCode(final String parentRoleCode) {
        this.parentRoleCode = parentRoleCode;
    }

    /**
     * caseload that this role belongs to, (NOMIS only)
     */
    @ApiModelProperty(value = "caseload that this role belongs to, (NOMIS only)")
    @JsonProperty("caseloadId")
    public String getCaseloadId() {
        return caseloadId;
    }

    public void setCaseloadId(final String caseloadId) {
        this.caseloadId = caseloadId;
    }

    /**
     * Staff username
     */
    @ApiModelProperty(required = true, value = "Staff username")
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Staff Id
     */
    @ApiModelProperty(required = true, value = "Staff Id")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(final Long staffId) {
        this.staffId = staffId;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class StaffUserRole {\n");

        sb.append("  roleId: ").append(roleId).append("\n");
        sb.append("  roleCode: ").append(roleCode).append("\n");
        sb.append("  roleName: ").append(roleName).append("\n");
        sb.append("  parentRoleCode: ").append(parentRoleCode).append("\n");
        sb.append("  caseloadId: ").append(caseloadId).append("\n");
        sb.append("  username: ").append(username).append("\n");
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
