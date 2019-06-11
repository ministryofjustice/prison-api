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
@ApiModel(description = "Summary data for a scheduled court event")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourtEvent {

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

    @ApiModelProperty(required = true, value = "The from agency description")
    private String fromAgencyDescription;

    @ApiModelProperty(required = true, value = "The agency code to which the transfer will be made (if an agency)")
    private String toAgency;

    @ApiModelProperty(required = true, value = "The to agency description")
    private String toAgencyDescription;

    @ApiModelProperty(required = true, value = "The date on which the event is scheduled to occur")
    private LocalDate eventDate;

    @ApiModelProperty(required = true, value = "The planned date and time of the start of the event")
    private LocalDateTime startTime;

    @ApiModelProperty(required = true, value = "The planned date and time of the end of the event")
    private LocalDateTime endTime;

    @ApiModelProperty(required = true, value = "The event class (from COURT_EVENTS)")
    private String eventClass;

    @ApiModelProperty(required = true, value = "The event type - always CRT")
    private String eventType;

    @ApiModelProperty(required = true, value = "The event sub-type (from COURT_EVENT_TYPE)")
    private String eventSubType;

    @ApiModelProperty(required = true, value = "The event status - either SCH (scheduled) or COMP (completed)")
    private String eventStatus;

    @ApiModelProperty(required = true, value = "Judge name, where available")
    private String judgeName;

    @ApiModelProperty(required = true, value = "The direction code (IN or OUT)")
    private String directionCode;

    @ApiModelProperty(required = true, value = "The comment text stored against this event")
    private String commentText;

    @ApiModelProperty(required = true, value = "The booking active flag - either Y or N from offender bookings")
    private String bookingActiveFlag;

    @ApiModelProperty(required = true, value = "The booking in or out status - either IN or OUT")
    private String bookingInOutStatus;
}