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

/**
 * Offender Sentence Detail
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Sentence Calculation")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceCalc<S extends BaseSentenceCalcDates> {
    @ApiModelProperty(required = true, value = "Offender booking id.", position = 0, example = "12341321")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Offender Unique Reference", position = 1, example = "A1000AA")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "First Name", position = 2, example = "John")
    @NotBlank
    private String firstName;

    @ApiModelProperty(required = true, value = "Last Name", position = 3, example = "Smith")
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "Agency Id", position = 4, example = "LEI")
    @NotBlank
    private String agencyLocationId;

    @ApiModelProperty(required = true, value = "Is this the most recent active booking", position = 5, example = "true")
    @NotNull
    private Boolean mostRecentActiveBooking;

    @ApiModelProperty(value = "Offender Sentence Detail Information", position = 10)
    private S sentenceDetail;

}
