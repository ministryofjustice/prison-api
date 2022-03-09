package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@ApiModel(description = "Summary data for a scheduled court event")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourtEvent {

    @ApiModelProperty(required = true, value = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Date and time the record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @ApiModelProperty(required = true, value = "The internal event ID", example = "12343434")
    private Long eventId;

    @ApiModelProperty(required = true, value = "The agency code ", example = "LEI")
    private String fromAgency;

    @ApiModelProperty(required = true, value = "The from agency description", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @ApiModelProperty(required = true, value = "The agency code to which the transfer will be made (if an agency)", example = "LEEDCC")
    private String toAgency;

    @ApiModelProperty(required = true, value = "The to agency description", example = "Leeds Crown Court")
    private String toAgencyDescription;

    @ApiModelProperty(required = true, value = "The date on which the event is scheduled to occur", example = "2019-12-01")
    private LocalDate eventDate;

    @ApiModelProperty(required = true, value = "The planned date and time of the start of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T14:00:00")
    private LocalDateTime startTime;

    @ApiModelProperty(required = true, value = "The planned date and time of the end of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T14:40:00")
    private LocalDateTime endTime;

    @ApiModelProperty(required = true, value = "The event class (from COURT_EVENTS)", example = "EXT_MOV")
    private String eventClass;

    @ApiModelProperty(required = true, value = "The event type", example = "CRT")
    private String eventType;

    @ApiModelProperty(required = true, value = "The event sub-type", example = "DP")
    private String eventSubType;

    @ApiModelProperty(required = true, value = "The event status - either SCH (scheduled) or COMP (completed)", example = "SCH")
    private String eventStatus;

    @ApiModelProperty(required = true, value = "Judge name, where available", example = "Harris")
    private String judgeName;

    @ApiModelProperty(required = true, value = "The direction code (IN or OUT)", example = "IN")
    private String directionCode;

    @ApiModelProperty(required = true, value = "The comment text stored against this event", example = "Restricted access to parking level")
    private String commentText;

    @ApiModelProperty(required = true, value = "The booking active flag", example = "true")
    private boolean bookingActiveFlag;

    @ApiModelProperty(required = true, value = "The booking in or out status - either IN or OUT", example = "OUT")
    private String bookingInOutStatus;
}