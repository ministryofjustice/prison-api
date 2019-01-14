package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Prisoner Custody Status
 **/
@SuppressWarnings("unused")
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

    @NotBlank
    @ApiModelProperty(required = true, value = "Description of the agency travelling from")
    private String fromAgencyDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency travelling to")
    private String toAgency;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description of the agency travelling to")
    private String toAgencyDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "ADM(ission), REL(ease) or TRN(sfer)")
    private String movementType;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description of the movement type")
    private String movementTypeDescription;

    @ApiModelProperty(required = true, value = "IN or OUT")
    @NotBlank
    private String directionCode;

    @ApiModelProperty(required = true, value = "Movement time")
    private LocalTime movementTime;

    @ApiModelProperty(required = true, value = "Description of movement reason")
    private String movementReason;

}
