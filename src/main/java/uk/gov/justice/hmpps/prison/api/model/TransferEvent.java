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
@ApiModel(description = "A scheduled offender movement event")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferEvent {

    @ApiModelProperty(required = true, value = "Offender number(NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Date and time the record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @ApiModelProperty(required = true, value = "The internal event ID", example = "1232323")
    private Long eventId;

    @ApiModelProperty(required = true, value = "The agency code from which the event will start", example = "LEI")
    private String fromAgency;

    @ApiModelProperty(required = true, value = "The from agency description", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @ApiModelProperty(required = true, value = "The agency code to which the transfer will be made (if an agency)", example = "MDI")
    private String toAgency;

    @ApiModelProperty(required = true, value = "The to agency description", example = "HMP MOORLANDS")
    private String toAgencyDescription;

    @ApiModelProperty(required = true, value = "The destination city when available", example = "DONCASTER")
    private String toCity;

    @ApiModelProperty(required = true, value = "The event status - either SCH or COMP", example = "SCH")
    private String eventStatus;

    @ApiModelProperty(required = true, value = "The event class - from OFFENDER_IND_SCHEDULES", example = "EXT_MOV")
    private String eventClass;

    @ApiModelProperty(required = true, value = "The event type - from OFFENDER_IND_SCHEDULES", example = "TRN")
    private String eventType;

    @ApiModelProperty(required = true, value = "The event sub-type - from OFFENDER_IND_SCHEDULES", example = "PP")
    private String eventSubType;

    @ApiModelProperty(required = true, value = "The date on which the event is scheduled to occur", example = "2019-01-01")
    private LocalDate eventDate;

    @ApiModelProperty(required = true, value = "The planned date and time of the start of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime startTime;

    @ApiModelProperty(required = true, value = "The planned date and time of the end of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime endTime;

    @ApiModelProperty(required = true, value = "The outcome reason code - from offender_ind_schedules", example = "CO")
    private String outcomeReasonCode;

    @ApiModelProperty(required = true, value = "The name of the judge where known", example = "")
    private String judgeName;

    @ApiModelProperty(required = true, value = "Engagement code", example = "ENG")
    private String engagementCode;

    @ApiModelProperty(required = true, value = "The escort code", example = "DEF2")
    private String escortCode;

    @ApiModelProperty(required = true, value = "The performance code", example = "PERF1")
    private String performanceCode;

    @ApiModelProperty(required = true, value = "The direction code (IN or OUT)", example = "IN")
    private String directionCode;

    @ApiModelProperty(required = true, value = "The booking active flag", example = "true")
    private boolean bookingActiveFlag;

    @ApiModelProperty(required = true, value = "The booking in or out status - either IN or OUT from offender bookings", example = "OUT")
    private String bookingInOutStatus;
}
