package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Data
public class PrivilegeDetail {
    @ApiModelProperty(required = true, value = "Offender booking identifier.")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Effective date of IEP level.")
    @NotNull
    private LocalDate iepDate;

    @ApiModelProperty(value = "Effective date & time of IEP level.")
    private LocalDateTime iepTime;

    @ApiModelProperty(required = true, value = "Identifier of Agency this privilege entry is associated with.")
    @NotBlank
    private String agencyId;

    @ApiModelProperty(required = true, value = "The IEP level (e.g. Basic, Standard or Enhanced).")
    @NotBlank
    private String iepLevel;

    @ApiModelProperty(value = "Further details relating to this privilege entry.")
    private String comments;

    @ApiModelProperty(value = "Identifier of user related to this privilege entry.")
    private String userId;
}
