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
import javax.validation.constraints.Size;
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
    @ApiModelProperty(value = "The description of the institution where this activity was based", example = "Moorland (HMP & YOI)")
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

    @ApiModelProperty(value = "End reason code")
    private String endReasonCode;

    @ApiModelProperty(value = "End reason description")
    private String endReasonDescription;

    @ApiModelProperty(value = "End comment")
    @Size(max = 240, message = "End comment text must be a maximum of 240 characters")
    private String endCommentText;

    @NotBlank
    @ApiModelProperty(value = "Whether the offender is currently registered to do this activity")
    private Boolean isCurrentActivity;

}
