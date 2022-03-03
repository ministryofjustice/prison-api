package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Schema(description = "Access Role")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AccessRole {

    @Schema(description = "internal role id")
    private Long roleId;

    @Schema(required = true, description = "unique code for the access role")
    @NotBlank
    private String roleCode;

    @Schema(description = "name of the access role")
    private String roleName;

    @Schema(description = "role code of the parent role")
    private String parentRoleCode;

    @Schema(description = "ADMIN or GENERAL")
    private String roleFunction;

}
