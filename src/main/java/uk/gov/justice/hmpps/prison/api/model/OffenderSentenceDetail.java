package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Offender Sentence Detail
 **/
@SuppressWarnings("unused")
@Schema(description = "Offender Sentence Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class OffenderSentenceDetail extends OffenderSentenceCalc<SentenceCalcDates> {
    @Schema(requiredMode = REQUIRED, description = "Offender date of birth.")
    @NotNull
    private LocalDate dateOfBirth;
    @Schema(requiredMode = REQUIRED, description = "Agency Description")
    @NotBlank
    private String agencyLocationDesc;
    @Schema(requiredMode = REQUIRED, description = "Description of the location within the prison")
    @NotBlank
    private String internalLocationDesc;
    @Schema(description = "Identifier of facial image of offender.")
    private Long facialImageId;

    @Builder(builderMethodName = "offenderSentenceDetailBuilder")
    public OffenderSentenceDetail(@NotNull final Long bookingId, @NotNull Boolean mostRecentActiveBooking, @NotBlank final String offenderNo, @NotBlank final String firstName, @NotBlank final String lastName,
                                  @NotBlank final String agencyLocationId, @NotNull final LocalDate dateOfBirth, @NotBlank final String agencyLocationDesc,
                                  @NotBlank final String internalLocationDesc, final Long facialImageId, final SentenceCalcDates sentenceDetail) {
        super(bookingId, offenderNo, firstName, lastName, agencyLocationId, mostRecentActiveBooking, sentenceDetail);
        this.dateOfBirth = dateOfBirth;
        this.agencyLocationDesc = agencyLocationDesc;
        this.internalLocationDesc = internalLocationDesc;
        this.facialImageId = facialImageId;
    }
}
