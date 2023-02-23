package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;

/**
 * Staff Role
 **/
@SuppressWarnings("unused")
@Schema(description = "Staff Role")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class StaffRole {
    @NotBlank
    private String role;

    private String roleDescription;

    public StaffRole(@NotBlank String role, String roleDescription) {
        this.role = role;
        this.roleDescription = roleDescription;
    }

    public StaffRole() {
    }

    /**
     * A code that defines staff member's role at agency.
     */
    @Schema(required = true, description = "A code that defines staff member's role at agency.")
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
    @Schema(description = "Description of staff member's role at agency.")
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
