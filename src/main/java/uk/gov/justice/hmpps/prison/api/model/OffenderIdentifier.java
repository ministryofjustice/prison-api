package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Offender Identifier
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Identifier")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderIdentifier {
    @NotBlank
    @ApiModelProperty(required = true, value = "Type of offender identifier", example = "PNC", position = 0)
    private String type;

    @NotBlank
    @ApiModelProperty(required = true, value = "The value of the offender identifier", example = "1231/XX/121", position = 1)
    private String value;

    @ApiModelProperty(value = "The offender number for this identifier", example = "A1234AB", position = 2)
    private String offenderNo;

    @ApiModelProperty(value = "The booking ID for this identifier", example = "1231223", position = 3)
    private Long bookingId;

    @ApiModelProperty(value = "Issuing Authority Information", example = "Important Auth", position = 4)
    private String issuedAuthorityText;

    @ApiModelProperty(value = "Date of issue", example = "2018-01-21", position = 5)
    private LocalDate issuedDate;

    @ApiModelProperty(value = "Related caseload type", example = "GENERAL", position = 6)
    private String caseloadType;
}
