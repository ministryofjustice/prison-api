package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Incentive &amp; Earned Privilege Summary
 **/
@SuppressWarnings("unused")
@Schema(description = "Incentive & Earned Privilege Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class PrivilegeSummary {

    @Schema(required = true, description = "Offender booking identifier.", example = "112321")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "The current IEP level (e.g. Basic, Standard or Enhanced).", example = "Basic", allowableValues = "Basic,Standard,Enhanced")
    @NotBlank
    private String iepLevel;

    @Schema(required = true, description = "Effective date of current IEP level.", example = "2019-01-24")
    @NotNull
    private LocalDate iepDate;

    @Schema(description = "Effective date & time of current IEP level.", example = "2019-01-24 15:00:00")
    private LocalDateTime iepTime;


    @Schema(required = true, description = "The number of days since last review.", example = "35")
    @NotNull
    private Long daysSinceReview;

    @Schema(description = "All IEP detail entries for the offender (most recent first).")
    @Default
    private List<PrivilegeDetail> iepDetails = new ArrayList<>();
}
