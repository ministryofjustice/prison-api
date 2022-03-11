package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Schema(description = "Summary data for a scheduled court event")
@Builder(toBuilder = true)
@Data
public class CourtEvent {

    @Schema(required = true, description = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @Schema(required = true, description = "Date and time the record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @Schema(required = true, description = "The internal event ID", example = "12343434")
    private Long eventId;

    @Schema(required = true, description = "The agency code ", example = "LEI")
    private String fromAgency;

    @Schema(required = true, description = "The from agency description", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @Schema(required = true, description = "The agency code to which the transfer will be made (if an agency)", example = "LEEDCC")
    private String toAgency;

    @Schema(required = true, description = "The to agency description", example = "Leeds Crown Court")
    private String toAgencyDescription;

    @Schema(required = true, description = "The date on which the event is scheduled to occur", example = "2019-12-01")
    private LocalDate eventDate;

    @Schema(required = true, description = "The planned date and time of the start of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T14:00:00")
    private LocalDateTime startTime;

    @Schema(required = true, description = "The planned date and time of the end of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T14:40:00")
    private LocalDateTime endTime;

    @Schema(required = true, description = "The event class (from COURT_EVENTS)", example = "EXT_MOV")
    private String eventClass;

    @Schema(required = true, description = "The event type", example = "CRT")
    private String eventType;

    @Schema(required = true, description = "The event sub-type", example = "DP")
    private String eventSubType;

    @Schema(required = true, description = "The event status - either SCH (scheduled) or COMP (completed)", example = "SCH")
    private String eventStatus;

    @Schema(required = true, description = "Judge name, where available", example = "Harris")
    private String judgeName;

    @Schema(required = true, description = "The direction code (IN or OUT)", example = "IN")
    private String directionCode;

    @Schema(required = true, description = "The comment text stored against this event", example = "Restricted access to parking level")
    private String commentText;

    @Schema(required = true, description = "The booking active flag", example = "true")
    private boolean bookingActiveFlag;

    @Schema(required = true, description = "The booking in or out status - either IN or OUT", example = "OUT")
    private String bookingInOutStatus;

    public CourtEvent(String offenderNo, LocalDateTime createDateTime, Long eventId, String fromAgency, String fromAgencyDescription, String toAgency, String toAgencyDescription, LocalDate eventDate, LocalDateTime startTime, LocalDateTime endTime, String eventClass, String eventType, String eventSubType, String eventStatus, String judgeName, String directionCode, String commentText, boolean bookingActiveFlag, String bookingInOutStatus) {
        this.offenderNo = offenderNo;
        this.createDateTime = createDateTime;
        this.eventId = eventId;
        this.fromAgency = fromAgency;
        this.fromAgencyDescription = fromAgencyDescription;
        this.toAgency = toAgency;
        this.toAgencyDescription = toAgencyDescription;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventClass = eventClass;
        this.eventType = eventType;
        this.eventSubType = eventSubType;
        this.eventStatus = eventStatus;
        this.judgeName = judgeName;
        this.directionCode = directionCode;
        this.commentText = commentText;
        this.bookingActiveFlag = bookingActiveFlag;
        this.bookingInOutStatus = bookingInOutStatus;
    }

    public CourtEvent() {}
}
