package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Offender Sentence Detail
 **/
@SuppressWarnings("unused")
@Schema(description = "Offender Sentence Calculation")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceCalc<S extends BaseSentenceCalcDates> {
    @Schema(requiredMode = REQUIRED, description = "Offender booking id.", example = "12341321")
    @NotNull
    private Long bookingId;

    @Schema(requiredMode = REQUIRED, description = "Offender Unique Reference", example = "A1000AA")
    @NotBlank
    private String offenderNo;

    @Schema(requiredMode = REQUIRED, description = "First Name", example = "John")
    @NotBlank
    private String firstName;

    @Schema(requiredMode = REQUIRED, description = "Last Name", example = "Smith")
    @NotBlank
    private String lastName;

    @Schema(requiredMode = REQUIRED, description = "Agency Id", example = "LEI")
    @NotBlank
    private String agencyLocationId;

    @Schema(requiredMode = REQUIRED, description = "Is this the most recent active booking", example = "true")
    @NotNull
    private Boolean mostRecentActiveBooking;

    @Schema(description = "Offender Sentence Detail Information")
    private S sentenceDetail;

}
