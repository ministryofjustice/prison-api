package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Incentive & Earned Privilege Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class PrivilegeDetail {
    @Schema(required = true, description = "Offender booking identifier.")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Effective date of IEP level.")
    @NotNull
    private LocalDate iepDate;

    @Schema(description = "Effective date & time of IEP level.")
    private LocalDateTime iepTime;

    @Schema(required = true, description = "Identifier of Agency this privilege entry is associated with.")
    @NotBlank
    private String agencyId;

    @Schema(required = true, description = "The IEP level (e.g. Basic, Standard or Enhanced).")
    @NotBlank
    private String iepLevel;

    @Schema(description = "Further details relating to this privilege entry.")
    private String comments;

    @Schema(description = "Identifier of user related to this privilege entry.")
    private String userId;
}
