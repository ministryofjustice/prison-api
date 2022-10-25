package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearing.OicHearingType;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "OicHearingRequest")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class OicHearingRequest {
    @Schema(required = true, description = "When the hearing is scheduled for", example = "15-06-2020T09:03:11")
    @NotNull
    private LocalDateTime dateTimeOfHearing;

    @Schema(required = true, description = "The id to indicate where the hearing will take place. Note: This will be an agency's internal location id")
    @NotNull
    private Long hearingLocationId;

    @Schema(required = true, description = "OIC hearing type (currently GOV_ADULT or GOV_YOI)")
    @NotNull
    private OicHearingType oicHearingType;
}

