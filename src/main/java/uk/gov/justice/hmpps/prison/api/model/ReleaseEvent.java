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
@ApiModel(description = "Summary data for a scheduled offender release")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReleaseEvent {

    @ApiModelProperty(required = true, value = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Date and time the record was created in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime createDateTime;

    @ApiModelProperty(required = true, value = "The internal event ID", example = "1234556")
    private Long eventId;

    @ApiModelProperty(required = true, value = "The agency code from which the release will be made", example = "LEI")
    private String fromAgency;

    @ApiModelProperty(required = true, value = "The agency description", example = "HMP LEEDS")
    private String fromAgencyDescription;

    @ApiModelProperty(required = true, value = "The planned release date", example = "2019-12-01")
    private LocalDate releaseDate;

    @ApiModelProperty(required = true, value = "The approved release date", example = "2019-12-01")
    private LocalDate approvedReleaseDate;

    @ApiModelProperty(required = true, value = "The event class - usually EXT_MOV", example = "EXT_MOV")
    private String eventClass;

    @ApiModelProperty(required = true, value = "The event status - either SCH (scheduled) or COMP (completed)", example = "SCH")
    private String eventStatus;

    @ApiModelProperty(required = true, value = "The movement type code - from OFFENDER_IND_SCHEDULE", example = "REL")
    private String movementTypeCode;

    @ApiModelProperty(required = true, value = "The movement type description from reference data", example = "Release at end of sentence")
    private String movementTypeDescription;

    @ApiModelProperty(required = true, value = "The movement reason code - from OFFENDER_IND_SCHEDULE", example = "DD")
    private String movementReasonCode;

    @ApiModelProperty(required = true, value = "The movement reason description from reference data", example = "Release")
    private String movementReasonDescription;

    @ApiModelProperty(required = true, value = "Any comment text entered against this event", example = "Notes relating to this release")
    private String commentText;

    @ApiModelProperty(required = true, value = "The booking active flag", example = "true")
    private boolean bookingActiveFlag;

    @ApiModelProperty(required = true, value = "The booking in or out status - either IN or OUT", example = "OUT")
    private String bookingInOutStatus;
}
