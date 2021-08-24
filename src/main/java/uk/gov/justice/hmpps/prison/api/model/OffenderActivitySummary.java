package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Offender activity")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderActivitySummary {
    @NotNull
    private Long bookingId;

    @NotNull
    @ApiModelProperty(value = "The id of the institution where this activity was based", example = "MDI")
    private String agencyLocationId;

    @NotNull
    @ApiModelProperty(value = "The description of the institution where this activity was based")
    private String agencyLocationDescription;

    @NotBlank
    @ApiModelProperty(value = "The description of the activity")
    private String description;

    @NotNull
    @ApiModelProperty(value = "When the offender started this activity")
    private LocalDate startDate;

    @NotNull
    @ApiModelProperty(value = "When the offender stopped this activity")
    private LocalDate endDate;

}
