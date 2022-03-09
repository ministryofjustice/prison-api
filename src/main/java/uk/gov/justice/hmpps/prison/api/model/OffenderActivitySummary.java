package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Offender activity")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderActivitySummary {
    @NotNull
    private Long bookingId;

    @NotNull
    @Schema(description = "The id of the institution where this activity was based", example = "MDI")
    private String agencyLocationId;

    @NotNull
    @Schema(description = "The description of the institution where this activity was based", example = "Moorland (HMP & YOI)")
    private String agencyLocationDescription;

    @NotBlank
    @Schema(description = "The description of the activity")
    private String description;

    @NotNull
    @Schema(description = "When the offender started this activity")
    private LocalDate startDate;

    @NotNull
    @Schema(description = "When the offender stopped this activity")
    private LocalDate endDate;

    @Schema(description = "End reason code")
    private String endReasonCode;

    @Schema(description = "End reason description")
    private String endReasonDescription;

    @Schema(description = "End comment")
    @Size(max = 240, message = "End comment text must be a maximum of 240 characters")
    private String endCommentText;

    @NotBlank
    @Schema(description = "Whether the offender is currently registered to do this activity")
    private Boolean isCurrentActivity;

}
