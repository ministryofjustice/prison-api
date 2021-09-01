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
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Incentive &amp; Earned Privilege Summary
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Incentive & Earned Privilege Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class PrivilegeSummary {

    @ApiModelProperty(required = true, value = "Offender booking identifier.", example = "112321", position = 1)
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "The current IEP level (e.g. Basic, Standard or Enhanced).", example = "Basic", allowableValues = "Basic,Standard,Enhanced", position = 2)
    @NotBlank
    private String iepLevel;

    @ApiModelProperty(required = true, value = "Effective date of current IEP level.", example = "2019-01-24", position = 3)
    @NotNull
    private LocalDate iepDate;

    @ApiModelProperty(value = "Effective date & time of current IEP level.", example = "2019-01-24 15:00:00", position = 4)
    private LocalDateTime iepTime;


    @ApiModelProperty(required = true, value = "The number of days since last review.", example = "35", position = 5)
    @NotNull
    private Long daysSinceReview;

    @ApiModelProperty(value = "All IEP detail entries for the offender (most recent first).", position = 6)
    private List<PrivilegeDetail> iepDetails;
}
