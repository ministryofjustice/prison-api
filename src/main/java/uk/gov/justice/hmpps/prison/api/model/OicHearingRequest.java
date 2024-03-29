package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearing.OicHearingType;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "OicHearingRequest")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class OicHearingRequest {
    @Schema(requiredMode = REQUIRED, description = "When the hearing is scheduled for", example = "15-06-2020T09:03:11")
    @NotNull
    private LocalDateTime dateTimeOfHearing;

    @Schema(requiredMode = REQUIRED, description = "The id to indicate where the hearing will take place. Note: This will be an agency's internal location id")
    @NotNull
    private Long hearingLocationId;

    @Schema(requiredMode = REQUIRED, description = "OIC hearing type (currently GOV_ADULT or GOV_YOI)")
    @NotNull
    private OicHearingType oicHearingType;

    @Schema(description = "optional adjudicator username")
    private String adjudicator;

    @Schema(description = "optional comment text")
    private String commentText;
}

