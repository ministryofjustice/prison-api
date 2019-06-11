package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@ApiModel(description = "Summary data for a completed movement")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovementSummary {

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender number (NOMS ID)")
    private String offenderNo;

    @NotNull
    @ApiModelProperty(required = true, value = "Timestamp when the external movement record was created")
    private LocalDateTime createDateTime;

    @ApiModelProperty(required = true, value = "The internal event ID")
    private Long eventId;

    @ApiModelProperty(required = true, value = "Agency travelling from")
    private String fromAgency;

    @ApiModelProperty(required = true, value = "Description of the agency travelling from")
    private String fromAgencyDescription;

    @ApiModelProperty(required = true, value = "Agency travelling to")
    private String toAgency;

    @ApiModelProperty(required = true, value = "Description of the agency travelling to")
    private String toAgencyDescription;

    @ApiModelProperty(value = "City offender was received from")
    private String fromCity;

    @ApiModelProperty(value = "City offender was sent to")
    private String toCity;

    @ApiModelProperty(value = "The arresting agency location ID")
    private String arrestAgencyLocId;

    @ApiModelProperty(value = "Internal schedule type")
    private String internalScheduleType;

    @ApiModelProperty(value = "Internal schedule reason code")
    private String internalScheduleReasonCode;

    @ApiModelProperty(value = "To prov stat code - from offender_external_movements")
    private String toProvStatCode;

    @ApiModelProperty(value = "The escort code")
    private String escortCode;

    @ApiModelProperty(value = "The escort text")
    private String escortText;

    @ApiModelProperty(required = true, value = "CRT (court), ADM (admission), REL(release) or TRN(transfer)")
    private String movementType;

    @ApiModelProperty(required = true, value = "Description of the movement type")
    private String movementTypeDescription;

    @ApiModelProperty(required = true, value = "IN or OUT")
    private String directionCode;

    @ApiModelProperty(required = true, value = "Movement date and time")
    private LocalDateTime movementTime;

    @ApiModelProperty(required = true, value = "Description of movement reason")
    private String movementReason;

    @ApiModelProperty(value = "Comment")
    private String commentText;
}
