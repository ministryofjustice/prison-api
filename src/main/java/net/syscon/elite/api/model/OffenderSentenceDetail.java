package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

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
@ToString
public class OffenderSentenceDetail extends OffenderSentenceCalc<SentenceDetail> {
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
    public OffenderSentenceDetail(@NotNull Long bookingId, @NotBlank String offenderNo, @NotBlank String firstName, @NotBlank String lastName, @NotBlank String agencyLocationId, @NotNull Long bookingId1, @NotBlank String offenderNo1, @NotBlank String firstName1, @NotBlank String lastName1, @NotNull LocalDate dateOfBirth, @NotBlank String agencyLocationId1, @NotBlank String agencyLocationDesc, @NotBlank String internalLocationDesc, Long facialImageId, SentenceDetail sentenceDetail) {
        super(bookingId, offenderNo, firstName, lastName, agencyLocationId, sentenceDetail);
        this.dateOfBirth = dateOfBirth;
        this.agencyLocationDesc = agencyLocationDesc;
        this.internalLocationDesc = internalLocationDesc;
        this.facialImageId = facialImageId;
    }
}
