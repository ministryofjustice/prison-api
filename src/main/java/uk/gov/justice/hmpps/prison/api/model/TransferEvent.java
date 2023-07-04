package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@SuppressWarnings("unused")
@Schema(description = "A scheduled offender movement event")
@Builder(toBuilder = true)
@Data
public class TransferEvent {

    @Schema(requiredMode = REQUIRED, description = "Offender number(NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @Schema(requiredMode = REQUIRED, description = "Date and time the record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @Schema(requiredMode = REQUIRED, description = "The internal event ID", example = "1232323")
    private Long eventId;

    @Schema(requiredMode = REQUIRED, description = "The agency code from which the event will start", example = "LEI")
    private String fromAgency;

    @Schema(requiredMode = REQUIRED, description = "The from agency description", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @Schema(requiredMode = REQUIRED, description = "The agency code to which the transfer will be made (if an agency)", example = "MDI")
    private String toAgency;

    @Schema(requiredMode = REQUIRED, description = "The to agency description", example = "HMP MOORLANDS")
    private String toAgencyDescription;

    @Schema(requiredMode = REQUIRED, description = "The destination city when available", example = "DONCASTER")
    private String toCity;

    @Schema(requiredMode = REQUIRED, description = "The event status - either SCH or COMP", example = "SCH")
    private String eventStatus;

    @Schema(requiredMode = REQUIRED, description = "The event class - from OFFENDER_IND_SCHEDULES", example = "EXT_MOV")
    private String eventClass;

    @Schema(requiredMode = REQUIRED, description = "The event type - from OFFENDER_IND_SCHEDULES", example = "TRN")
    private String eventType;

    @Schema(requiredMode = REQUIRED, description = "The event sub-type - from OFFENDER_IND_SCHEDULES", example = "PP")
    private String eventSubType;

    @Schema(requiredMode = REQUIRED, description = "The date on which the event is scheduled to occur", example = "2019-01-01")
    private LocalDate eventDate;

    @Schema(requiredMode = REQUIRED, description = "The planned date and time of the start of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime startTime;

    @Schema(requiredMode = REQUIRED, description = "The planned date and time of the end of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime endTime;

    @Schema(requiredMode = REQUIRED, description = "The outcome reason code - from offender_ind_schedules", example = "CO")
    private String outcomeReasonCode;

    @Schema(requiredMode = REQUIRED, description = "The name of the judge where known", example = "")
    private String judgeName;

    @Schema(requiredMode = REQUIRED, description = "Engagement code", example = "ENG")
    private String engagementCode;

    @Schema(requiredMode = REQUIRED, description = "The escort code", example = "DEF2")
    private String escortCode;

    @Schema(requiredMode = REQUIRED, description = "The performance code", example = "PERF1")
    private String performanceCode;

    @Schema(requiredMode = REQUIRED, description = "The direction code (IN or OUT)", example = "IN")
    private String directionCode;

    @Schema(requiredMode = REQUIRED, description = "The booking active flag", example = "true")
    private boolean bookingActiveFlag;

    @Schema(requiredMode = REQUIRED, description = "The booking in or out status - either IN or OUT from offender bookings", example = "OUT")
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
