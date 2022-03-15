package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Schema(description = "A scheduled offender movement event")
@Builder(toBuilder = true)
@Data
public class TransferEvent {

    @Schema(required = true, description = "Offender number(NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @Schema(required = true, description = "Date and time the record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @Schema(required = true, description = "The internal event ID", example = "1232323")
    private Long eventId;

    @Schema(required = true, description = "The agency code from which the event will start", example = "LEI")
    private String fromAgency;

    @Schema(required = true, description = "The from agency description", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @Schema(required = true, description = "The agency code to which the transfer will be made (if an agency)", example = "MDI")
    private String toAgency;

    @Schema(required = true, description = "The to agency description", example = "HMP MOORLANDS")
    private String toAgencyDescription;

    @Schema(required = true, description = "The destination city when available", example = "DONCASTER")
    private String toCity;

    @Schema(required = true, description = "The event status - either SCH or COMP", example = "SCH")
    private String eventStatus;

    @Schema(required = true, description = "The event class - from OFFENDER_IND_SCHEDULES", example = "EXT_MOV")
    private String eventClass;

    @Schema(required = true, description = "The event type - from OFFENDER_IND_SCHEDULES", example = "TRN")
    private String eventType;

    @Schema(required = true, description = "The event sub-type - from OFFENDER_IND_SCHEDULES", example = "PP")
    private String eventSubType;

    @Schema(required = true, description = "The date on which the event is scheduled to occur", example = "2019-01-01")
    private LocalDate eventDate;

    @Schema(required = true, description = "The planned date and time of the start of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime startTime;

    @Schema(required = true, description = "The planned date and time of the end of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime endTime;

    @Schema(required = true, description = "The outcome reason code - from offender_ind_schedules", example = "CO")
    private String outcomeReasonCode;

    @Schema(required = true, description = "The name of the judge where known", example = "")
    private String judgeName;

    @Schema(required = true, description = "Engagement code", example = "ENG")
    private String engagementCode;

    @Schema(required = true, description = "The escort code", example = "DEF2")
    private String escortCode;

    @Schema(required = true, description = "The performance code", example = "PERF1")
    private String performanceCode;

    @Schema(required = true, description = "The direction code (IN or OUT)", example = "IN")
    private String directionCode;

    @Schema(required = true, description = "The booking active flag", example = "true")
    private boolean bookingActiveFlag;

    @Schema(required = true, description = "The booking in or out status - either IN or OUT from offender bookings", example = "OUT")
    private String bookingInOutStatus;

    public TransferEvent(String offenderNo, LocalDateTime createDateTime, Long eventId, String fromAgency, String fromAgencyDescription, String toAgency, String toAgencyDescription, String toCity, String eventStatus, String eventClass, String eventType, String eventSubType, LocalDate eventDate, LocalDateTime startTime, LocalDateTime endTime, String outcomeReasonCode, String judgeName, String engagementCode, String escortCode, String performanceCode, String directionCode, boolean bookingActiveFlag, String bookingInOutStatus) {
        this.offenderNo = offenderNo;
        this.createDateTime = createDateTime;
        this.eventId = eventId;
        this.fromAgency = fromAgency;
        this.fromAgencyDescription = fromAgencyDescription;
        this.toAgency = toAgency;
        this.toAgencyDescription = toAgencyDescription;
        this.toCity = toCity;
        this.eventStatus = eventStatus;
        this.eventClass = eventClass;
        this.eventType = eventType;
        this.eventSubType = eventSubType;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.outcomeReasonCode = outcomeReasonCode;
        this.judgeName = judgeName;
        this.engagementCode = engagementCode;
        this.escortCode = escortCode;
        this.performanceCode = performanceCode;
        this.directionCode = directionCode;
        this.bookingActiveFlag = bookingActiveFlag;
        this.bookingInOutStatus = bookingInOutStatus;
    }

    public TransferEvent() {}
}
