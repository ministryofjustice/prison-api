package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Schema(description = "Summary data for a scheduled offender release")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReleaseEvent {

    @Schema(required = true, description = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @Schema(required = true, description = "Date and time the record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @Schema(required = true, description = "The internal event ID", example = "1234556")
    private Long eventId;

    @Schema(required = true, description = "The agency code from which the release will be made", example = "LEI")
    private String fromAgency;

    @Schema(required = true, description = "The agency description", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @Schema(required = true, description = "The planned release date", example = "2019-12-01")
    private LocalDate releaseDate;

    @Schema(required = true, description = "The approved release date", example = "2019-12-01")
    private LocalDate approvedReleaseDate;

    @Schema(required = true, description = "The event class - usually EXT_MOV", example = "EXT_MOV")
    private String eventClass;

    @Schema(required = true, description = "The event status - either SCH (scheduled) or COMP (completed)", example = "SCH")
    private String eventStatus;

    @Schema(required = true, description = "The movement type code - from OFFENDER_IND_SCHEDULE", example = "REL")
    private String movementTypeCode;

    @Schema(required = true, description = "The movement type description from reference data", example = "Release at end of sentence")
    private String movementTypeDescription;

    @Schema(required = true, description = "The movement reason code - from OFFENDER_IND_SCHEDULE", example = "DD")
    private String movementReasonCode;

    @Schema(required = true, description = "The movement reason description from reference data", example = "Release")
    private String movementReasonDescription;

    @Schema(required = true, description = "Any comment text entered against this event", example = "Notes relating to this release")
    private String commentText;

    @Schema(required = true, description = "The booking active flag", example = "true")
    private boolean bookingActiveFlag;

    @Schema(required = true, description = "The booking in or out status - either IN or OUT", example = "OUT")
    private String bookingInOutStatus;
}
