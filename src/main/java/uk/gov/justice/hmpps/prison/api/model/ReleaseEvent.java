package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@SuppressWarnings("unused")
@Schema(description = "Summary data for a scheduled offender release")
@Builder(toBuilder = true)
@Data
public class ReleaseEvent {

    @Schema(requiredMode = REQUIRED, description = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @Schema(requiredMode = REQUIRED, description = "Date and time the record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @Schema(requiredMode = REQUIRED, description = "The internal event ID", example = "1234556")
    private Long eventId;

    @Schema(requiredMode = REQUIRED, description = "The agency code from which the release will be made", example = "LEI")
    private String fromAgency;

    @Schema(requiredMode = REQUIRED, description = "The agency description", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @Schema(requiredMode = REQUIRED, description = "The planned release date", example = "2019-12-01")
    private LocalDate releaseDate;

    @Schema(requiredMode = REQUIRED, description = "The approved release date", example = "2019-12-01")
    private LocalDate approvedReleaseDate;

    @Schema(requiredMode = REQUIRED, description = "The event class - usually EXT_MOV", example = "EXT_MOV")
    private String eventClass;

    @Schema(requiredMode = REQUIRED, description = "The event status - either SCH (scheduled) or COMP (completed)", example = "SCH")
    private String eventStatus;

    @Schema(requiredMode = REQUIRED, description = "The movement type code - from OFFENDER_IND_SCHEDULE", example = "REL")
    private String movementTypeCode;

    @Schema(requiredMode = REQUIRED, description = "The movement type description from reference data", example = "Release at end of sentence")
    private String movementTypeDescription;

    @Schema(requiredMode = REQUIRED, description = "The movement reason code - from OFFENDER_IND_SCHEDULE", example = "DD")
    private String movementReasonCode;

    @Schema(requiredMode = REQUIRED, description = "The movement reason description from reference data", example = "Release")
    private String movementReasonDescription;

    @Schema(requiredMode = REQUIRED, description = "Any comment text entered against this event", example = "Notes relating to this release")
    private String commentText;

    @Schema(requiredMode = REQUIRED, description = "The booking active flag", example = "true")
    private boolean bookingActiveFlag;

    @Schema(requiredMode = REQUIRED, description = "The booking in or out status - either IN or OUT", example = "OUT")
    private String bookingInOutStatus;

    public ReleaseEvent(String offenderNo, LocalDateTime createDateTime, Long eventId, String fromAgency, String fromAgencyDescription, LocalDate releaseDate, LocalDate approvedReleaseDate, String eventClass, String eventStatus, String movementTypeCode, String movementTypeDescription, String movementReasonCode, String movementReasonDescription, String commentText, boolean bookingActiveFlag, String bookingInOutStatus) {
        this.offenderNo = offenderNo;
        this.createDateTime = createDateTime;
        this.eventId = eventId;
        this.fromAgency = fromAgency;
        this.fromAgencyDescription = fromAgencyDescription;
        this.releaseDate = releaseDate;
        this.approvedReleaseDate = approvedReleaseDate;
        this.eventClass = eventClass;
        this.eventStatus = eventStatus;
        this.movementTypeCode = movementTypeCode;
        this.movementTypeDescription = movementTypeDescription;
        this.movementReasonCode = movementReasonCode;
        this.movementReasonDescription = movementReasonDescription;
        this.commentText = commentText;
        this.bookingActiveFlag = bookingActiveFlag;
        this.bookingInOutStatus = bookingInOutStatus;
    }

    public ReleaseEvent() {}
}
