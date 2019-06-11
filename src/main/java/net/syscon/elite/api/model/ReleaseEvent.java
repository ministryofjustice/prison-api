package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@ApiModel(description = "Summary data for a scheduled offender release")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReleaseEvent {

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender number (NOMS ID)")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Date and time the record was created")
    private LocalDateTime createDateTime;

    @ApiModelProperty(required = true, value = "The internal event ID")
    private Long eventId;

    @ApiModelProperty(required = true, value = "The agency code from which the release will be made")
    private String fromAgency;

    @ApiModelProperty(required = true, value = "The agency description")
    private String fromAgencyDescription;

    @ApiModelProperty(required = true, value = "The planned release date")
    private LocalDate releaseDate;

    @ApiModelProperty(required = true, value = "The approved release date")
    private LocalDate approvedReleaseDate;

    @ApiModelProperty(required = true, value = "The event class - usually EXT_MOV")
    private String eventClass;

    @ApiModelProperty(required = true, value = "The event status - either SCH (scheduled) or COMP (completed)")
    private String eventStatus;

    @ApiModelProperty(required = true, value = "The movement type code - from OFFENDER_IND_SCHEDULE")
    private String movementTypeCode;

    @ApiModelProperty(required = true, value = "The movement type description from reference data")
    private String movementTypeDescription;

    @ApiModelProperty(required = true, value = "The movement reason code - from OFFENDER_IND_SCHEDULE")
    private String movementReasonCode;

    @ApiModelProperty(required = true, value = "The movement reason description from reference data")
    private String movementReasonDescription;

    @ApiModelProperty(required = true, value = "Any comment text entered against this event")
    private String commentText;

    @ApiModelProperty(required = true, value = "The booking active flag - either Y or N from offender bookings")
    private String bookingActiveFlag;

    @ApiModelProperty(required = true, value = "The booking in or out status - either IN or OUT")
    private String bookingInOutStatus;
}
