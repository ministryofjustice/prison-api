package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Schema(description = "Summary data for a completed movement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
public class MovementSummary {

    @Schema(required = true, description = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @Schema(required = true, description = "Timestamp when the external movement record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @Schema(required = true, description = "The internal event ID", example = "1223232")
    private Long eventId;

    @Schema(required = true, description = "Agency travelling from", example = "LEI")
    private String fromAgency;

    @Schema(required = true, description = "Description of the agency travelling from", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @Schema(required = true, description = "Agency travelling to", example = "MDI")
    private String toAgency;

    @Schema(required = true, description = "Description of the agency travelling to", example = "HMP MOORLANDS")
    private String toAgencyDescription;

    @Schema(description = "City offender was received from", example = "LEEDS")
    private String fromCity;

    @Schema(description = "City offender was sent to", example = "DONCASTER")
    private String toCity;

    @Schema(description = "The arresting agency location ID", example = "SYPOL")
    private String arrestAgencyLocId;

    @Schema(description = "Internal schedule type", example = "")
    private String internalScheduleType;

    @Schema(description = "Internal schedule reason code", example = "")
    private String internalScheduleReasonCode;

    @Schema(description = "To prov stat code - from offender_external_movements", example = "")
    private String toProvStatCode;

    @Schema(description = "The escort code", example = "PECS123")
    private String escortCode;

    @Schema(description = "The escort text", example = "Secure van")
    private String escortText;

    @Schema(required = true, description = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = "ADM,CRT,REL,TAP,TRN")
    private String movementType;

    @Schema(required = true, description = "Description of the movement type", example = "Admission")
    private String movementTypeDescription;

    @Schema(required = true, description = "IN or OUT", example = "IN")
    private String directionCode;

    @Schema(required = true, description = "Movement date and time in Europe/London local time format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime movementTime;

    @Schema(required = true, description = "Description of movement reason", example = "Convicted at court")
    private String movementReason;

    @Schema(description = "Comment", example = "This is a free text comment")
    private String commentText;

    public MovementSummary(String offenderNo, LocalDateTime createDateTime, Long eventId, String fromAgency, String fromAgencyDescription, String toAgency, String toAgencyDescription, String fromCity, String toCity, String arrestAgencyLocId, String internalScheduleType, String internalScheduleReasonCode, String toProvStatCode, String escortCode, String escortText, String movementType, String movementTypeDescription, String directionCode, LocalDateTime movementTime, String movementReason, String commentText) {
        this.offenderNo = offenderNo;
        this.createDateTime = createDateTime;
        this.eventId = eventId;
        this.fromAgency = fromAgency;
        this.fromAgencyDescription = fromAgencyDescription;
        this.toAgency = toAgency;
        this.toAgencyDescription = toAgencyDescription;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.arrestAgencyLocId = arrestAgencyLocId;
        this.internalScheduleType = internalScheduleType;
        this.internalScheduleReasonCode = internalScheduleReasonCode;
        this.toProvStatCode = toProvStatCode;
        this.escortCode = escortCode;
        this.escortText = escortText;
        this.movementType = movementType;
        this.movementTypeDescription = movementTypeDescription;
        this.directionCode = directionCode;
        this.movementTime = movementTime;
        this.movementReason = movementReason;
        this.commentText = commentText;
    }

    public MovementSummary() {}
}
