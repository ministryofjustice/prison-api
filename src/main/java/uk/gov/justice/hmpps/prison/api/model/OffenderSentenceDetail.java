package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Offender Sentence Detail
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Sentence Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class OffenderSentenceDetail extends OffenderSentenceCalc<SentenceCalcDates> {
    @ApiModelProperty(required = true, value = "Offender date of birth.", position = 5)
    @NotNull
    private LocalDate dateOfBirth;
    @ApiModelProperty(required = true, value = "Agency Description", position = 6)
    @NotBlank
    private String agencyLocationDesc;
    @ApiModelProperty(required = true, value = "Description of the location within the prison", position = 7)
    @NotBlank
    private String internalLocationDesc;
    @ApiModelProperty(value = "Identifier of facial image of offender.", position = 8)
    private Long facialImageId;

    @Builder(builderMethodName = "offenderSentenceDetailBuilder")
    public OffenderSentenceDetail(@NotNull final Long bookingId, boolean mostRecentActiveBooking, @NotBlank final String offenderNo, @NotBlank final String firstName, @NotBlank final String lastName,
                                  @NotBlank final String agencyLocationId, @NotNull final LocalDate dateOfBirth, @NotBlank final String agencyLocationDesc,
                                  @NotBlank final String internalLocationDesc, final Long facialImageId, final SentenceCalcDates sentenceDetail) {
        super(bookingId, offenderNo, firstName, lastName, agencyLocationId, mostRecentActiveBooking, sentenceDetail);
        this.dateOfBirth = dateOfBirth;
        this.agencyLocationDesc = agencyLocationDesc;
        this.internalLocationDesc = internalLocationDesc;
        this.facialImageId = facialImageId;
    }
}
