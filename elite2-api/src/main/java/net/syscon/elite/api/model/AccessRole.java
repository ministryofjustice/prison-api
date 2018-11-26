package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

/**
 * Access Role
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Access Role")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AccessRole {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    private Long roleId;

    @NotBlank
    private String roleCode;

    private String roleName;

    private String parentRoleCode;

    private String roleFunction;

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
      * internal role id
      */
    @ApiModelProperty(value = "internal role id")
    @JsonProperty("roleId")
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    /**
      * unique code for the access role
      */
    @ApiModelProperty(required = true, value = "unique code for the access role")
    @JsonProperty("roleCode")
    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    /**
      * name of the access role
      */
    @ApiModelProperty(value = "name of the access role")
    @JsonProperty("roleName")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
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

    public void setParentRoleCode(String parentRoleCode) {
        this.parentRoleCode = parentRoleCode;
    }

    /**
      * ADMIN or GENERAL
      */
    @ApiModelProperty(value = "ADMIN or GENERAL")
    @JsonProperty("roleFunction")
    public String getRoleFunction() {
        return roleFunction;
    }

    public void setRoleFunction(String roleFunction) {
        this.roleFunction = roleFunction;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class AccessRole {\n");
        
        sb.append("  roleId: ").append(roleId).append("\n");
        sb.append("  roleCode: ").append(roleCode).append("\n");
        sb.append("  roleName: ").append(roleName).append("\n");
        sb.append("  parentRoleCode: ").append(parentRoleCode).append("\n");
        sb.append("  roleFunction: ").append(roleFunction).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
