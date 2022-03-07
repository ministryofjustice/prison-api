package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Access Role")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AccessRole {

    @ApiModelProperty(value = "internal role id")
    private Long roleId;

    @ApiModelProperty(required = true, value = "unique code for the access role")
    @NotBlank
    private String roleCode;

    @ApiModelProperty(value = "name of the access role")
    private String roleName;

    @ApiModelProperty(value = "role code of the parent role")
    private String parentRoleCode;

    @ApiModelProperty(value = "ADMIN or GENERAL")
    private String roleFunction;

}
