package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
    @Schema(required = true, description = "Offender booking id.", example = "12341321")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Offender Unique Reference", example = "A1000AA")
    @NotBlank
    private String offenderNo;

    @Schema(required = true, description = "First Name", example = "John")
    @NotBlank
    private String firstName;

    @Schema(required = true, description = "Last Name", example = "Smith")
    @NotBlank
    private String lastName;

    @Schema(required = true, description = "Agency Id", example = "LEI")
    @NotBlank
    private String agencyLocationId;

    @Schema(required = true, description = "Is this the most recent active booking", example = "true")
    @NotNull
    private Boolean mostRecentActiveBooking;

    @Schema(description = "Offender Sentence Detail Information")
    private S sentenceDetail;

}
