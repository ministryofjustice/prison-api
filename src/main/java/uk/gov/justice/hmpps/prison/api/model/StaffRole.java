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
import java.util.HashMap;
import java.util.Map;

/**
 * Staff Role
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Staff Role")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StaffRole {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotBlank
    private String role;

    private String roleDescription;

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
     * A code that defines staff member's role at agency.
     */
    @ApiModelProperty(required = true, value = "A code that defines staff member's role at agency.")
    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    /**
     * Description of staff member's role at agency.
     */
    @ApiModelProperty(value = "Description of staff member's role at agency.")
    @JsonProperty("roleDescription")
    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(final String roleDescription) {
        this.roleDescription = roleDescription;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class StaffRole {\n");

        sb.append("  role: ").append(role).append("\n");
        sb.append("  roleDescription: ").append(roleDescription).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
