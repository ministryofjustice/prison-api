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
@EqualsAndHashCode
@ToString
@Data
public class OffenderSentenceCalc<S extends BaseSentenceDetail> {
    @ApiModelProperty(required = true, value = "Offender booking id.")
    @NotNull
    protected Long bookingId;

    @ApiModelProperty(required = true, value = "Offender Unique Reference")
    @NotBlank
    protected String offenderNo;

    @ApiModelProperty(required = true, value = "First Name")
    @NotBlank
    protected String firstName;

    @ApiModelProperty(required = true, value = "Last Name")
    @NotBlank
    protected String lastName;

    @ApiModelProperty(required = true, value = "Agency Id")
    @NotBlank
    protected String agencyLocationId;

    @ApiModelProperty(value = "Offender Sentence Detail Information")
    protected S sentenceDetail;

}
