package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
@ApiModel(description = "Summary data for a completed movement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovementSummary {

    @ApiModelProperty(required = true, value = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Timestamp when the external movement record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @ApiModelProperty(required = true, value = "The internal event ID", example = "1223232")
    private Long eventId;

    @ApiModelProperty(required = true, value = "Agency travelling from", example = "LEI")
    private String fromAgency;

    @ApiModelProperty(required = true, value = "Description of the agency travelling from", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @ApiModelProperty(required = true, value = "Agency travelling to", example = "MDI")
    private String toAgency;

    @ApiModelProperty(required = true, value = "Description of the agency travelling to", example = "HMP MOORLANDS")
    private String toAgencyDescription;

    @ApiModelProperty(value = "City offender was received from", example = "LEEDS")
    private String fromCity;

    @ApiModelProperty(value = "City offender was sent to", example = "DONCASTER")
    private String toCity;

    @ApiModelProperty(value = "The arresting agency location ID", example = "SYPOL")
    private String arrestAgencyLocId;

    @ApiModelProperty(value = "Internal schedule type", example = "")
    private String internalScheduleType;

    @ApiModelProperty(value = "Internal schedule reason code", example = "")
    private String internalScheduleReasonCode;

    @ApiModelProperty(value = "To prov stat code - from offender_external_movements", example = "")
    private String toProvStatCode;

    @ApiModelProperty(value = "The escort code", example = "PECS123")
    private String escortCode;

    @ApiModelProperty(value = "The escort text", example = "Secure van")
    private String escortText;

    @ApiModelProperty(required = true, value = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = "ADM,CRT,REL,TAP,TRN")
    private String movementType;

    @ApiModelProperty(required = true, value = "Description of the movement type", example = "Admission")
    private String movementTypeDescription;

    @ApiModelProperty(required = true, value = "IN or OUT", example = "IN")
    private String directionCode;

    @ApiModelProperty(required = true, value = "Movement date and time in Europe/London local time format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime movementTime;

    @ApiModelProperty(required = true, value = "Description of movement reason", example = "Convicted at court")
    private String movementReason;

    @ApiModelProperty(value = "Comment", example = "This is a free text comment")
    private String commentText;
}
