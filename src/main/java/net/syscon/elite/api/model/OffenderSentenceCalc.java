package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

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
public class OffenderSentenceCalc<S extends BaseSentenceDetail> {
    @ApiModelProperty(required = true, value = "Offender booking id.", position = 0)
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Offender Unique Reference", position = 1)
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "First Name", position = 2)
    @NotBlank
    private String firstName;

    @ApiModelProperty(required = true, value = "Last Name", position = 3)
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "Agency Id", position = 4)
    @NotBlank
    private String agencyLocationId;

    @ApiModelProperty(value = "Offender Sentence Detail Information", position = 10)
    private S sentenceDetail;

}
