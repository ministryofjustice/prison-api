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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Prisoner Custody Status
 **/
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Prisoner Custody Status")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Movement {

    @NotBlank
    @ApiModelProperty(required = true, value = "Display Prisoner Number (UK is NOMS ID)")
    private String offenderNo;

    @NotNull
    @ApiModelProperty(required = true, value = "Timestamp when the external movement record was created")
    private LocalDateTime createDateTime;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency travelling from")
    private String fromAgency;

    @ApiModelProperty(required = true, value = "Description of the agency travelling from")
    private String fromAgencyDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency travelling to")
    private String toAgency;

    @ApiModelProperty(required = true, value = "Description of the agency travelling to")
    private String toAgencyDescription;

    @ApiModelProperty(value = "City offender was received from")
    private String fromCity;

    @ApiModelProperty(value = "City offender was sent to")
    private String toCity;

    @NotBlank
    @ApiModelProperty(required = true, value = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = "ADM,CRT,REL,TAP,TRN")
    private String movementType;

    @ApiModelProperty(required = true, value = "Description of the movement type")
    private String movementTypeDescription;

    @ApiModelProperty(required = true, value = "IN or OUT")
    @NotBlank
    private String directionCode;

    @ApiModelProperty(required = true, value = "Movement date")
    private LocalDate movementDate;

    @ApiModelProperty(required = true, value = "Movement time")
    private LocalTime movementTime;

    @ApiModelProperty(required = true, value = "Description of movement reason")
    private String movementReason;

    @ApiModelProperty(value = "Comment")
    private String commentText;
}
